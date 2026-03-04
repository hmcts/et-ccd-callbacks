#!/usr/bin/env bash

set -eu

echo "📥 Importing CCD Definitions via API"
echo "===================================="

# Get the directory of this script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="${SCRIPT_DIR}/../.."

# Use preview-specific token utilities
source "${SCRIPT_DIR}/utils/auth-utils.sh"

echo "🔐 Getting authentication tokens..."
echo "Retrieving IDAM user token"
USER_TOKEN=$(get_user_token)
echo "Retrieving S2S service token for CCD Gateway"
SERVICE_TOKEN=$(get_service_token "ccd_gw")

# Define the Excel files to import (in order)
declare -a DEFINITION_FILES=(
    "ccd-definitions/jurisdictions/admin/xlsx/et-admin-ccd-config-preview.xlsx"
    "ccd-definitions/jurisdictions/england-wales/xlsx/et-englandwales-ccd-config-preview.xlsx"
    "ccd-definitions/jurisdictions/scotland/xlsx/et-scotland-ccd-config-preview.xlsx"
)

# Function to import a single CCD definition file
import_definition() {
    local file_path="$1"
    local full_path="${REPO_ROOT}/${file_path}"
    if [[ ! -f "${full_path}" ]]; then
        echo "❌ Error: Definition file not found: ${full_path}"
        return 1
    fi
    
    local file_size=$(du -h "${full_path}" | cut -f1)
    echo "  📋 Importing: $(basename "${file_path}") (${file_size})"
    local response
    response=$(curl -s -w "%{http_code}" \
        -X POST \
        "${CCD_DEFINITION_STORE_API_BASE_URL}/import" \
        -H "Authorization: Bearer ${USER_TOKEN}" \
        -H "ServiceAuthorization: ${SERVICE_TOKEN}" \
        -F "file=@${full_path}")
    
    local http_code="${response: -3}"
    local response_body="${response%???}"
    
    if [[ "${http_code}" == "201" ]] || [[ "${http_code}" == "200" ]]; then
        echo "    ✅ Successfully imported $(basename "${file_path}")"
    else
        echo "    ❌ Failed to import $(basename "${file_path}") (HTTP ${http_code})"
        echo "    Response: ${response_body}"
    fi
}

echo "📥 Importing CCD definition files..."

# Import each definition file
for file_path in "${DEFINITION_FILES[@]}"; do
    import_definition "${file_path}"
    echo ""
done

echo "✅ CCD definitions import completed!"

