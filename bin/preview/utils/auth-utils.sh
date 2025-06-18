#!/usr/bin/env bash

# Authentication utilities for preview environment
# These functions handle IDAM and S2S authentication for preview deployments

set -eu

# Get IDAM user token for preview environment
get_user_token() {
    local username="${CCD_ADMIN_USERNAME}"
    local password="${CCD_ADMIN_PASSWORD}"
    local idam_uri="${IDAM_API_BASE_URL}"
    local redirect_uri="http://localhost:3451/oauth2redirect"
    local client_id="ccd_gateway"
    local client_secret="ccd_gateway_secret"
    
    if [[ -z "${username}" || -z "${password}" ]]; then
        echo "❌ Error: CCD_ADMIN_USERNAME and CCD_ADMIN_PASSWORD must be set" >&2
        exit 1
    fi
    
    # Get authorization code
    local code
    code=$(curl -s -u "${username}:${password}" \
        -X POST \
        "${idam_uri}/oauth2/authorize?redirect_uri=${redirect_uri}&response_type=code&client_id=${client_id}" \
        -d "" | jq -r .code)
    
    if [[ "${code}" == "null" || -z "${code}" ]]; then
        echo "❌ Error: Failed to get authorization code from IDAM" >&2
        exit 1
    fi
    
    # Exchange code for token
    local token
    token=$(curl -s \
        -H "Content-Type: application/x-www-form-urlencoded" \
        -u "${client_id}:${client_secret}" \
        -X POST \
        "${idam_uri}/oauth2/token?code=${code}&redirect_uri=${redirect_uri}&grant_type=authorization_code" \
        -d "" | jq -r .access_token)
    
    if [[ "${token}" == "null" || -z "${token}" ]]; then
        echo "❌ Error: Failed to get access token from IDAM" >&2
        exit 1
    fi
    
    echo "${token}"
}

# Get service-to-service token
get_service_token() {
    local microservice="${1:-ccd_gw}"
    local s2s_uri="${SERVICE_AUTH_PROVIDER_API_BASE_URL}"
    
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
    
    local token
    token=$(curl -s \
        -X POST \
        "${s2s_uri}/lease" \
        -H "Content-Type: application/json" \
        -d "{
            \"microservice\": \"${microservice}\",
            \"oneTimePassword\": \"${s2s_secret}\"
        }" | jq -r .access_token)
    
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
    
    if [[ -z "${SERVICE_AUTH_PROVIDER_API_BASE_URL:-}" ]]; then
        missing_vars+=("SERVICE_AUTH_PROVIDER_API_BASE_URL")
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

