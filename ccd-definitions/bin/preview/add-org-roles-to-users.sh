#!/usr/bin/env bash

set -eu

echo "👥 Adding Organization Roles to Users"
echo "====================================="

# Get the directory of this script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Use preview-specific token utilities
source "${SCRIPT_DIR}/utils/auth-utils.sh"

echo "🔐 Getting authentication tokens..."
USER_TOKEN=$(get_user_token)
SERVICE_TOKEN=$(get_service_token "ccd_gw")

# Define users and their organizational roles
declare -A USER_ROLES=(
    ["et-acas-api@hmcts.net"]="caseworker-employment-api"
    ["etl-etjudge@justice.gov.uk"]="caseworker-employment-etjudge"
    ["etl-solicitor@justice.gov.uk"]="caseworker-employment-legalrep-solicitor"
    ["etl-admin@justice.gov.uk"]="caseworker-employment"
)

echo "📝 Adding organizational roles to users..."

# Function to add role to user
add_user_role() {
    local user_email="$1"
    local role_name="$2"
    
    echo "  Adding role '${role_name}' to user '${user_email}'"
    
    local response
    response=$(curl -s -w "%{http_code}" \
        -X PUT \
        "${CASE_USER_PROFILE_API_BASE_URL}/case-users/${user_email}/roles/${role_name}" \
        -H "Authorization: Bearer ${USER_TOKEN}" \
        -H "ServiceAuthorization: ${SERVICE_TOKEN}" \
        -H "Content-Type: application/json")
    
    local http_code="${response: -3}"
    local response_body="${response%???}"
    
    if [[ "${http_code}" == "201" ]] || [[ "${http_code}" == "200" ]]; then
        echo "    ✅ Role '${role_name}' added to '${user_email}' successfully"
    elif [[ "${http_code}" == "409" ]]; then
        echo "    ℹ️  Role '${role_name}' already assigned to '${user_email}'"
    else
        echo "    ❌ Failed to add role '${role_name}' to '${user_email}' (HTTP ${http_code})"
        echo "    Response: ${response_body}"
    fi
}

# Add roles to users
for user_email in "${!USER_ROLES[@]}"; do
    role_name="${USER_ROLES[$user_email]}"
    add_user_role "${user_email}" "${role_name}"
done

echo "✅ Organization roles assignment completed!"

