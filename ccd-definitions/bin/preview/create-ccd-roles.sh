#!/usr/bin/env bash

set -eu

echo "👥 Creating CCD Roles via API"
echo "============================="

# Get the directory of this script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
UTILS_DIR="${SCRIPT_DIR}/../utils"

# Use preview-specific token utilities
source "${SCRIPT_DIR}/utils/auth-utils.sh"

# Define ET CCD roles that need to be created
declare -a ET_ROLES=(
    "caseworker"
    "caseworker-employment"
    "caseworker-employment-api" 
    "caseworker-employment-englandwales"
    "caseworker-employment-scotland"
    "caseworker-employment-etjudge"
    "caseworker-employment-etjudge-englandwales"
    "caseworker-employment-etjudge-scotland"
    "caseworker-et-pcqextractor"
    "citizen"
    "caseworker-employment-legalrep-solicitor"
    "caseworker-approver"
    "caseworker-caa"
    "et-acas-api"
    "GS_profile"
    "caseworker-ras-validation"
    "caseworker-wa-task-configuration"
    "TTL_profile"
    "caseworker-wa"
    "caseworker-wa-task-officer"
    "caseworker-employment-etjudge-leeds"
    "caseworker-employment-leeds"
)

echo "🔐 Getting authentication tokens..."
USER_TOKEN=$(get_user_token)
SERVICE_TOKEN=$(get_service_token "ccd_gw")

echo "📝 Creating ${#ET_ROLES[@]} CCD roles..."

# Function to create a single role
create_role() {
    local role_name="$1"
    local security_classification="${2:-PUBLIC}"
    
    echo "  Creating role: ${role_name}"
    
    local response
    response=$(curl -s -w "%{http_code}" \
        -X PUT \
        "${CCD_DEFINITION_STORE_API_BASE_URL}/api/user-role" \
        -H "Authorization: Bearer ${USER_TOKEN}" \
        -H "ServiceAuthorization: ${SERVICE_TOKEN}" \
        -H "Content-Type: application/json" \
        -d "{
            \"role\": \"${role_name}\",
            \"security_classification\": \"${security_classification}\"
        }")
    
    local http_code="${response: -3}"
    local response_body="${response%???}"
    
    if [[ "${http_code}" == "201" ]] || [[ "${http_code}" == "200" ]]; then
        echo "    ✅ Role '${role_name}' created/updated successfully"
    elif [[ "${http_code}" == "409" ]]; then
        echo "    ℹ️  Role '${role_name}' already exists"
    else
        echo "    ❌ Failed to create role '${role_name}' (HTTP ${http_code})"
        echo "    Response: ${response_body}"
    fi
}

# Create all roles
for role in "${ET_ROLES[@]}"; do
    create_role "${role}"
done

echo "✅ CCD roles creation completed!"

