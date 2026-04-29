#!/usr/bin/env bash

# Authentication utilities for preview environment
# These functions handle IDAM and S2S authentication for preview deployments

set -eu

# Get IDAM user token for preview environment
get_user_token() {
      local username="${CCD_ADMIN_USERNAME:-}"
      local password="${CCD_ADMIN_PASSWORD:-}"
      local idam_uri="${IDAM_API_URL:-}"

    if [[ -z "${username}" || -z "${password}" ]]; then
        echo "❌ Error: CCD_ADMIN_USERNAME and CCD_ADMIN_PASSWORD must be set" >&2
        exit 1
    fi

    # Get access token
    local token=
    curl --silent --location "${idam_uri}/loginUser" \
    --header 'Content-Type: application/x-www-form-urlencoded' \
      --data-urlencode "username=${username}" \
       --data-urlencode "password=${password}" | jq -r .access_token

    echo "${token}"
}

get_user_token_from_email_password() {
      local username="${1:-}"
      local password="${2:-}"
      local idam_uri="${IDAM_API_URL:-}"

    if [[ -z "${username}" || -z "${password}" ]]; then
        echo "❌ Error: username and password must be provided" >&2
        exit 1
    fi

    # Get access token
    local token
    token=$(curl --silent --location "${idam_uri}/loginUser" \
    --header 'Content-Type: application/x-www-form-urlencoded' \
      --data-urlencode "username=${username}" \
       --data-urlencode "password=${password}" | jq -r .access_token)

    if [[ -z "${token}" || "${token}" == "null" ]]; then
        echo "❌ Error: Failed to get IDAM token for user '${username}'" >&2
        exit 1
    fi

    echo "${token}"
}

# Get Staff user token for preview environment
# Uses OAuth2 /o/token with manage-user/create-user/search-user scopes required by Ref Data API.
# Parameters are sent in the POST body (not the URL) to avoid curl mis-parsing '@' in the email.
#
# Uses the 'xuiwebapp' IDAM client because it has the required management scopes registered.
# XUI_IDAM_CLIENT_SECRET is injected by the Jenkinsfile from the rpx/mc-idam-client-secret vault.
get_staff_admin_token() {
      echo "🔐 Getting Staff Admin user token..." >&2
      local username="${ET_STAFF_USER_ADMIN_USER_NAME:-}"
      local password="${ET_STAFF_USER_ADMIN_USER_NAME_PASSWORD:-}"
      local idam_uri="${IDAM_API_URL:-}"
      local client_secret="${XUI_IDAM_CLIENT_SECRET:-}"
      local client_id="xuiwebapp"
      local redirect_uri="https://manage-case.aat.platform.hmcts.net/oauth2/callback"
      local scope="profile openid roles manage-user create-user search-user"

    if [[ -z "${username}" || -z "${password}" ]]; then
        echo "❌ Error: ET_STAFF_USER_ADMIN_USER_NAME and ET_STAFF_USER_ADMIN_USER_NAME_PASSWORD must be set" >&2
        exit 1
    fi

    if [[ -z "${client_secret}" ]]; then
        echo "❌ Error: XUI_IDAM_CLIENT_SECRET must be set (loaded from rpx/mc-idam-client-secret vault)" >&2
        exit 1
    fi

    # Get access token via OAuth2 password grant with required scopes.
    # Values are form-encoded into the body to safely handle special characters.
    local raw_response
    raw_response=$(curl --silent --show-error \
      -X POST "${idam_uri}/o/token" \
      -H "Content-Type: application/x-www-form-urlencoded" \
      --data-urlencode "grant_type=password" \
      --data-urlencode "redirect_uri=${redirect_uri}" \
      --data-urlencode "client_id=${client_id}" \
      --data-urlencode "client_secret=${client_secret}" \
      --data-urlencode "username=${username}" \
      --data-urlencode "password=${password}" \
      --data-urlencode "scope=${scope}")
    local token
    token=$(echo "${raw_response}" | jq -r .access_token 2>/dev/null)

    if [[ -z "${token}" || "${token}" == "null" ]]; then
        echo "❌ Error: Failed to get IDAM token for staff admin user '${username}'" >&2
        echo "Raw IDAM response: ${raw_response}" >&2
        exit 1
    fi

    echo "${token}"
}

# Get service-to-service token
get_service_token() {
    local microservice="${1:-ccd_gw}"
    local s2s_uri="${SERVICE_AUTH_PROVIDER_URL:-}"

    # Use appropriate S2S secret based on service
    local s2s_secret
    case "${microservice}" in
        "ccd_gw")
            s2s_secret="${API_GATEWAY_S2S_KEY:-}"
            ;;
        "ccd_data")
            s2s_secret="${DATA_STORE_S2S_KEY:-}"
            ;;
        "ccd_definition")
            s2s_secret="${DEFINITION_STORE_S2S_KEY:-}"
            ;;
        "ccd_admin")
            s2s_secret="${ADMIN_S2S_KEY:-}"
            ;;
        "xui_webapp")
            s2s_secret="${XUI_S2S_KEY:-}"
            ;;
        "et_cos")
            s2s_secret="${ET_COS_S2S_KEY:-}"
            ;;
        *)
            echo "❌ Error: Unknown microservice '${microservice}'" >&2
            exit 1
            ;;
    esac

    if [[ -z "${s2s_secret}" ]]; then
        echo "❌ Error: S2S secret for '${microservice}' not found in environment" >&2
        exit 1
    fi

    local token=$(
        curl --silent --location "${s2s_uri}/testing-support/lease" \
            --header 'Content-Type: application/json' \
            --data "{
                \"microservice\": \"${microservice}\",
                \"oneTimePassword\": \"${s2s_secret}\"
            }"
    )

    if [[ "${token}" == "null" || -z "${token}" ]]; then
        echo "❌ Error: Failed to get S2S token for '${microservice}'" >&2
        exit 1
    fi

    echo "${token}"
}

# Check if required environment variables are set
check_auth_env() {
    local missing_vars=()

    if [[ -z "${CCD_ADMIN_USERNAME:-}" ]]; then
        missing_vars+=("CCD_ADMIN_USERNAME")
    fi

    if [[ -z "${CCD_ADMIN_PASSWORD:-}" ]]; then
        missing_vars+=("CCD_ADMIN_PASSWORD")
    fi

    if [[ -z "${IDAM_API_BASE_URL:-}" ]]; then
        missing_vars+=("IDAM_API_BASE_URL")
    fi

    if [[ -z "${SERVICE_AUTH_PROVIDER_URL:-}" ]]; then
        missing_vars+=("SERVICE_AUTH_PROVIDER_URL")
    fi

    if [[ ${#missing_vars[@]} -gt 0 ]]; then
        echo "❌ Error: Missing required environment variables:" >&2
        printf '  %s\n' "${missing_vars[@]}" >&2
        exit 1
    fi
}

# Validate tokens are working
validate_tokens() {
    local user_token="$1"
    local service_token="$2"

    echo "🔍 Validating tokens..."

    # Test user token with user profile API
    local user_test
    user_test=$(curl -s -w "%{http_code}" \
        -X GET \
        "${CCD_USER_PROFILE_API_BASE_URL}/user-profile/users" \
        -H "Authorization: Bearer ${user_token}" \
        -H "ServiceAuthorization: ${service_token}")

    local http_code="${user_test: -3}"

    if [[ "${http_code}" == "200" ]] || [[ "${http_code}" == "201" ]]; then
        echo "  ✅ Tokens validated successfully"
    else
        echo "  ❌ Token validation failed (HTTP ${http_code})"
        exit 1
    fi
}

get_idam_id_from_token() {
    local user_token="${1:-}"

    if [[ -z "${user_token}" ]]; then
        echo "❌ Error: User token must be provided to get IDAM user ID" >&2
        exit 1
    fi

    local idam_user_id
    idam_user_id=$(curl --silent --show-error -X GET "${IDAM_API_URL}/details" \
        -H "accept: application/json" \
        -H "authorization: Bearer ${user_token}" | jq -r .id)
    echo "${idam_user_id}"
}