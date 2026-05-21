#!/usr/bin/env bash

set -eu

CURL_CONNECT_TIMEOUT_SECONDS=30
CURL_MAX_TIME_SECONDS=600
CURL_RETRY_COUNT=3
CURL_RETRY_DELAY_SECONDS=5

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
    local file_name
    file_name="$(basename "${file_path}")"
    if [[ ! -f "${full_path}" ]]; then
        echo "❌ Error: Definition file not found: ${full_path}"
        return 1
    fi

    local file_size=$(du -h "${full_path}" | cut -f1)
    local response_body_file
    response_body_file=$(mktemp)
    echo "  📋 Importing: ${file_name} (${file_size})"
    echo "    Using HTTP/1.1 with ${CURL_RETRY_COUNT} retries, ${CURL_CONNECT_TIMEOUT_SECONDS}s connect timeout, ${CURL_MAX_TIME_SECONDS}s max time"

    local http_code
    if http_code=$(curl --silent --show-error --location \
        --http1.1 \
        --retry "${CURL_RETRY_COUNT}" \
        --retry-delay "${CURL_RETRY_DELAY_SECONDS}" \
        --retry-all-errors \
        --connect-timeout "${CURL_CONNECT_TIMEOUT_SECONDS}" \
        --max-time "${CURL_MAX_TIME_SECONDS}" \
        --output "${response_body_file}" \
        --write-out "%{http_code}" \
        -X POST \
        "${CCD_DEFINITION_STORE_API_BASE_URL}/import" \
        -H "Authorization: Bearer ${USER_TOKEN}" \
        -H "ServiceAuthorization: ${SERVICE_TOKEN}" \
        -F "file=@${full_path}"); then
        :
    else
        local curl_exit_code="$?"
        echo "    ❌ curl failed to import ${file_name} (exit ${curl_exit_code})"
        if [[ -s "${response_body_file}" ]]; then
            echo "    Response: $(cat "${response_body_file}")"
        fi
        rm -f "${response_body_file}"
        return "${curl_exit_code}"
    fi

    local response_body
    response_body=$(cat "${response_body_file}")
    rm -f "${response_body_file}"

    if [[ "${http_code}" == "201" ]] || [[ "${http_code}" == "200" ]]; then
        echo "    ✅ Successfully imported ${file_name}"
    else
        echo "    ❌ Failed to import ${file_name} (HTTP ${http_code})"
        echo "    Response: ${response_body}"
        return 1
    fi
}

echo "📥 Importing CCD definition files..."

# Import each definition file
for file_path in "${DEFINITION_FILES[@]}"; do
    import_definition "${file_path}"
    echo ""
done

echo "✅ CCD definitions import completed!"
