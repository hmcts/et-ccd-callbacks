#!/usr/bin/env bash

set -eu

CURL_CONNECT_TIMEOUT_SECONDS=30
CURL_MAX_TIME_SECONDS=600
CURL_RETRY_COUNT=3
CURL_RETRY_DELAY_SECONDS=5

# The CCD definition-store creates ElasticSearch indices/aliases while importing
# searchable case types. ES can still be starting up when the import runs, which
# surfaces as an HTTP 400 with an "ElasticSearch initialisation" message. These
# are transient, so retry the whole import for this specific error.
IMPORT_ES_RETRY_COUNT="${IMPORT_ES_RETRY_COUNT:-10}"
IMPORT_ES_RETRY_DELAY_SECONDS="${IMPORT_ES_RETRY_DELAY_SECONDS:-15}"

echo "📥 Importing CCD Definitions via API"
echo "===================================="

# Get the directory of this script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="${SCRIPT_DIR}/../.."

# Use preview-specific token utilities
source "${SCRIPT_DIR}/utils/auth-utils.sh"
source "${SCRIPT_DIR}/utils/definition-store-db-utils.sh"

echo "⏳ Waiting for CCD definition-store schema..."
wait_for_definition_store_schema

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

# Returns 0 if the (HTTP code, response body) pair represents a transient
# ElasticSearch readiness error that is worth retrying.
is_transient_es_error() {
    local http_code="$1"
    local response_body="$2"

    [[ "${http_code}" == "400" || "${http_code}" == "500" || "${http_code}" == "503" ]] \
        && [[ "${response_body}" == *"ElasticSearch initialisation"* \
            || "${response_body}" == *"Elasticsearch initialisation"* \
            || "${response_body}" == *"check alias existence"* ]]
}

# Performs a single import attempt. Echoes "<http_code>|<response_body>" on a
# completed HTTP exchange, or returns the curl exit code on a transport failure.
import_definition_attempt() {
    local full_path="$1"
    local response_body_file
    response_body_file=$(mktemp)

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
        if [[ -s "${response_body_file}" ]]; then
            echo "    Response: $(cat "${response_body_file}")" >&2
        fi
        rm -f "${response_body_file}"
        return "${curl_exit_code}"
    fi

    local response_body
    response_body=$(cat "${response_body_file}")
    rm -f "${response_body_file}"
    echo "${http_code}|${response_body}"
}

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
    echo "  📋 Importing: ${file_name} (${file_size})"
    echo "    Using HTTP/1.1 with ${CURL_RETRY_COUNT} retries, ${CURL_CONNECT_TIMEOUT_SECONDS}s connect timeout, ${CURL_MAX_TIME_SECONDS}s max time"

    local attempt=1
    local max_attempts=$((IMPORT_ES_RETRY_COUNT + 1))
    while true; do
        local result http_code response_body
        if result=$(import_definition_attempt "${full_path}"); then
            :
        else
            local curl_exit_code="$?"
            echo "    ❌ curl failed to import ${file_name} (exit ${curl_exit_code})"
            return "${curl_exit_code}"
        fi

        http_code="${result%%|*}"
        response_body="${result#*|}"

        if [[ "${http_code}" == "201" ]] || [[ "${http_code}" == "200" ]]; then
            echo "    ✅ Successfully imported ${file_name}"
            return 0
        fi

        if is_transient_es_error "${http_code}" "${response_body}" && (( attempt < max_attempts )); then
            echo "    ⏳ ElasticSearch not ready yet for ${file_name} (HTTP ${http_code}, attempt ${attempt}/${max_attempts}), retrying in ${IMPORT_ES_RETRY_DELAY_SECONDS}s..."
            echo "    Response: ${response_body}"
            attempt=$((attempt + 1))
            sleep "${IMPORT_ES_RETRY_DELAY_SECONDS}"
            continue
        fi

        echo "    ❌ Failed to import ${file_name} (HTTP ${http_code})"
        echo "    Response: ${response_body}"
        return 1
    done
}

echo "📥 Importing CCD definition files..."

# Import each definition file
for file_path in "${DEFINITION_FILES[@]}"; do
    import_definition "${file_path}"
    echo ""
done

echo "✅ CCD definitions import completed!"
