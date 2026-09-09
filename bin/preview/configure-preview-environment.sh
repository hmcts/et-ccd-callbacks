#!/usr/bin/env bash

set -euo pipefail

PR_ID=${1:-${CHANGE_ID:-}}
DMN_BRANCH=${2:-master}
BPMN_BRANCH=${3:-master}
NAMESPACE=${PREVIEW_NAMESPACE:-et}
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

source "${SCRIPT_DIR}/utils/definition-store-db-utils.sh"

if [[ -z "${PR_ID}" ]]; then
  echo "Usage: $0 <pr-id> [dmn-branch] [bpmn-branch]"
  exit 1
fi

MARKER_NAME="et-cos-pr-${PR_ID}-configuration"

cleanup_disposable_dependencies() {
  if [[ -d ccd-definitions/node_modules || -d ccd-definitions/.yarn/cache ]]; then
    echo "Cleaning disposable CCD definition dependencies before workspace transfer"
    rm -rf \
      ccd-definitions/node_modules \
      ccd-definitions/.yarn/cache
  fi
}

trap cleanup_disposable_dependencies EXIT

remote_revision() {
  local repository=$1
  local branch=$2
  git ls-remote "${repository}" "refs/heads/${branch}" | awk 'NR == 1 { print $1 }'
}

configuration_fingerprint() {
  {
    # Hash the complete local configuration trees so changes to any directly or
    # transitively invoked setup utility invalidate the marker.
    git rev-parse \
      HEAD:ccd-definitions \
      HEAD:bin/preview \
      HEAD:bin/wa
    remote_revision https://github.com/hmcts/et-wa-task-configuration.git "${DMN_BRANCH}"
    remote_revision https://github.com/hmcts/wa-standalone-task-bpmn.git "${BPMN_BRANCH}"

    # Include non-secret inputs that select the users and backing services being
    # configured. Credentials are intentionally excluded: rotating a password
    # does not change the desired preview configuration and must not be exposed
    # through the marker fingerprint.
    printf '%s\n' \
      "preview_namespace=${NAMESPACE}" \
      "pr_id=${PR_ID}" \
      "et_caseofficer_username=${ET_CASEOFFICER_USERNAME:-}" \
      "et_caseworker_username=${ET_CASEWORKER_USER_NAME:-}" \
      "camunda_base_url=${CAMUNDA_BASE_URL:-https://camunda-et-cos-pr-${PR_ID}.preview.platform.hmcts.net}" \
      "ccd_definition_store_api_base_url=${CCD_DEFINITION_STORE_API_BASE_URL:-https://ccd-definition-store-et-cos-pr-${PR_ID}.preview.platform.hmcts.net}" \
      "ref_data_url=${REF_DATA_URL:-https://rd-caseworker-ref-api-et-cos-pr-${PR_ID}.preview.platform.hmcts.net}" \
      "role_assignment_url=${ROLE_ASSIGNMENT_URL:-https://am-role-assignment-et-cos-pr-${PR_ID}.preview.platform.hmcts.net}" \
      "idam_api_url=${IDAM_API_URL:-}" \
      "service_auth_provider_url=${SERVICE_AUTH_PROVIDER_URL:-}" \
      "service_auth_provider_api_base_url=${SERVICE_AUTH_PROVIDER_API_BASE_URL:-}" \
      "source_et_cos_db_host=${ET_COS_AAT_DB_HOST:-${SOURCE_ET_COS_DB_HOST:-et-cos-postgres-v15-aat.postgres.database.azure.com}}" \
      "source_et_cos_db_port=${ET_COS_AAT_DB_PORT:-${SOURCE_ET_COS_DB_PORT:-5432}}" \
      "source_et_cos_db_name=${ET_COS_AAT_DB_NAME:-${SOURCE_ET_COS_DB_NAME:-et_cos}}" \
      "source_et_cos_db_user=${ET_COS_AAT_DB_USER_NAME:-${SOURCE_ET_COS_DB_USER_NAME:-pgadmin}}" \
      "source_et_cos_db_conn_options=${ET_COS_AAT_DB_CONN_OPTIONS:-${SOURCE_ET_COS_DB_CONN_OPTIONS:-sslmode=require}}" \
      "target_et_cos_db_host=${ET_COS_PREVIEW_DB_HOST:-et-preview.postgres.database.azure.com}" \
      "target_et_cos_db_port=${ET_COS_PREVIEW_DB_PORT:-5432}" \
      "target_et_cos_db_name=${ET_COS_PREVIEW_DB_NAME:-pr-${PR_ID}-et_cos}" \
      "target_et_cos_db_user=${ET_COS_PREVIEW_DB_USER_NAME:-hmcts}" \
      "target_et_cos_db_conn_options=${ET_COS_PREVIEW_DB_CONN_OPTIONS:-sslmode=require}" \
      "definition_store_db_host=${CCD_DEFINITION_STORE_PREVIEW_DB_HOST:-et-preview.postgres.database.azure.com}" \
      "definition_store_db_port=${CCD_DEFINITION_STORE_PREVIEW_DB_PORT:-5432}" \
      "definition_store_db_name=${CCD_DEFINITION_STORE_PREVIEW_DB_NAME:-pr-${PR_ID}-definition-store}" \
      "definition_store_db_user=${CCD_DEFINITION_STORE_PREVIEW_DB_USER_NAME:-hmcts}" \
      "definition_store_db_conn_options=${CCD_DEFINITION_STORE_PREVIEW_DB_CONN_OPTIONS:-sslmode=require}"
  } | sha256sum | awk '{ print $1 }'
}

FINGERPRINT=$(configuration_fingerprint)
EXISTING_FINGERPRINT=$(kubectl \
  --namespace "${NAMESPACE}" \
  get configmap "${MARKER_NAME}" \
  --output jsonpath='{.data.fingerprint}' \
  2>/dev/null || true)

if [[ "${EXISTING_FINGERPRINT}" == "${FINGERPRINT}" ]]; then
  wait_for_definition_store_schema "${PR_ID}"
  if definition_store_case_types_ready; then
    echo "Preview environment configuration is current (${FINGERPRINT}); skipping import."
    exit 0
  fi

  echo "Preview configuration marker is current, but expected ET case types are missing; running configuration again."
fi

for hostname in \
  "camunda-et-cos-pr-${PR_ID}.preview.platform.hmcts.net" \
  "ccd-definition-store-et-cos-pr-${PR_ID}.preview.platform.hmcts.net" \
  "rd-caseworker-ref-api-et-cos-pr-${PR_ID}.preview.platform.hmcts.net" \
  "am-role-assignment-et-cos-pr-${PR_ID}.preview.platform.hmcts.net"
do
  attempt=0
  until getent hosts "${hostname}" > /dev/null; do
    if [[ "${attempt}" -ge 6 ]]; then
      echo "DNS did not resolve for ${hostname}"
      exit 1
    fi

    attempt=$((attempt + 1))
    echo "Waiting for DNS for ${hostname} (retry ${attempt}/6)"
    sleep 5
  done
done

CAMUNDA_URL=${CAMUNDA_BASE_URL:-https://camunda-et-cos-pr-${PR_ID}.preview.platform.hmcts.net}
./bin/preview/wait-for-http-endpoint.sh "${CAMUNDA_URL}/engine-rest/version" "Camunda"
./bin/wa/pull-latest-dmn-files.sh "${DMN_BRANCH}"
./bin/wa/pull-latest-camunda-wa-files.sh "${BPMN_BRANCH}"

(
  cd ccd-definitions
  yarn install
  yarn setup
  yarn scripts:preview "${PR_ID}"
)

echo "Importing ET COS reference data"
./bin/preview/import-ref-data.sh "${PR_ID}"

echo "Creating CCD roles"
./bin/preview/create-ccd-roles.sh

echo "Importing CCD definitions"
./bin/preview/import-ccd-definitions.sh

echo "Setting up WA users and roles"
./bin/wa/add-org-roles-to-users.sh

kubectl create configmap "${MARKER_NAME}" \
  --namespace "${NAMESPACE}" \
  --from-literal="fingerprint=${FINGERPRINT}" \
  --from-literal="configured-at=$(date -u '+%Y-%m-%dT%H:%M:%SZ')" \
  --dry-run=client \
  --output yaml | kubectl apply --filename -

echo "Preview environment configuration completed (${FINGERPRINT})."
