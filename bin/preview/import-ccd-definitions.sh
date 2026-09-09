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
IMPORT_ES_RETRY_COUNT="${IMPORT_ES_RETRY_COUNT:-20}"
IMPORT_ES_RETRY_DELAY_SECONDS="${IMPORT_ES_RETRY_DELAY_SECONDS:-15}"
IMPORT_ES_MAX_RETRY_DELAY_SECONDS="${IMPORT_ES_MAX_RETRY_DELAY_SECONDS:-60}"
IMPORT_ES_INITIAL_WAIT_SECONDS="${IMPORT_ES_INITIAL_WAIT_SECONDS:-30}"
LOG_OUTPUT_MAX_LENGTH="${LOG_OUTPUT_MAX_LENGTH:-200}"

truncate_log_output() {
    local value="$1"
    if ((${#value} > LOG_OUTPUT_MAX_LENGTH)); then
        echo "${value:0:LOG_OUTPUT_MAX_LENGTH}...[truncated]"
    else
        echo "${value}"
    fi
}

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

if (( IMPORT_ES_INITIAL_WAIT_SECONDS > 0 )); then
    echo "⏳ Waiting ${IMPORT_ES_INITIAL_WAIT_SECONDS}s before CCD definition import to allow dependent services to settle..."
    sleep "${IMPORT_ES_INITIAL_WAIT_SECONDS}"
fi

log_definition_store_health() {
    local endpoint_path="$1"
    local health_url="${CCD_DEFINITION_STORE_API_BASE_URL}${endpoint_path}"
    local response_file
    response_file=$(mktemp)

    local http_code
    http_code=$(curl --silent --show-error --location \
        --http1.1 \
        --connect-timeout 10 \
        --max-time 30 \
        --output "${response_file}" \
        --write-out "%{http_code}" \
        -H "Authorization: Bearer ${USER_TOKEN}" \
        -H "ServiceAuthorization: ${SERVICE_TOKEN}" \
        "${health_url}" || true)

    local response_body
    response_body=$(cat "${response_file}")
    rm -f "${response_file}"

    if [[ -n "${http_code}" && "${http_code}" != "000" ]]; then
        echo "ℹ️  definition-store health probe ${endpoint_path} -> HTTP ${http_code}"
        if [[ -n "${response_body}" ]]; then
            echo "ℹ️  health response: $(truncate_log_output "${response_body}")"
        fi
    else
        echo "ℹ️  definition-store health probe ${endpoint_path} unavailable"
    fi
}

# Best-effort diagnostics to aid triage when ES readiness keeps failing.
log_definition_store_health "/health"
log_definition_store_health "/actuator/health"

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
            echo "    Response: $(truncate_log_output "$(cat "${response_body_file}")")" >&2
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
            local retry_sleep_seconds
            retry_sleep_seconds=$((IMPORT_ES_RETRY_DELAY_SECONDS * attempt))
            if (( retry_sleep_seconds > IMPORT_ES_MAX_RETRY_DELAY_SECONDS )); then
                retry_sleep_seconds="${IMPORT_ES_MAX_RETRY_DELAY_SECONDS}"
            fi

            echo "    ⏳ ElasticSearch not ready yet for ${file_name} (HTTP ${http_code}, attempt ${attempt}/${max_attempts}), retrying in ${retry_sleep_seconds}s..."
            echo "    Response: $(truncate_log_output "${response_body}")"
            attempt=$((attempt + 1))
            sleep "${retry_sleep_seconds}"
            continue
        fi

        echo "    ❌ Failed to import ${file_name} (HTTP ${http_code})"
        echo "    Response: $(truncate_log_output "${response_body}")"
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
