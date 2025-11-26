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

# Get Staff user token for preview environment
get_staff_admin_token() {
      echo "🔐 Getting Staff Admin user token..."
      local username="${ET_STAFF_USER_ADMIN_USER_NAME:-}"
      local password="${ET_STAFF_USER_ADMIN_USER_NAME_PASSWORD:-}"
      local idam_uri="${IDAM_API_URL:-}"

    if [[ -z "${username}" || -z "${password}" ]]; then
        echo "❌ Error: ET_STAFF_USER_ADMIN_USER_NAME and ET_STAFF_USER_ADMIN_USER_NAME_PASSWORD must be set" >&2
        exit 1
    fi

    # Get access token
    local token=
    curl --silent --location "${idam_uri}/loginUser" \
    --header 'Content-Type: application/x-www-form-urlencoded' \
      --data-urlencode "username=${username}" \
       --data-urlencode "password=${password}" | jq -r .access_token

    echo "Admin token: ${token}"
}

# Get service-to-service token
get_service_token() {
    echo "🔐 Getting S2S service token for ${microservice}..."
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

