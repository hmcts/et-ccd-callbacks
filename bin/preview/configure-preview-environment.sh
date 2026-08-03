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

remote_revision() {
  local repository=$1
  local branch=$2
  git ls-remote "${repository}" "refs/heads/${branch}" | awk 'NR == 1 { print $1 }'
}

configuration_fingerprint() {
  {
    git rev-parse HEAD:ccd-definitions
    git hash-object \
      bin/preview/configure-preview-environment.sh \
      bin/preview/create-ccd-roles.sh \
      bin/preview/import-ccd-definitions.sh \
      bin/preview/import-ref-data.sh \
      bin/preview/utils/definition-store-db-utils.sh \
      bin/wa/add-org-roles-to-users.sh
    remote_revision https://github.com/hmcts/et-wa-task-configuration.git "${DMN_BRANCH}"
    remote_revision https://github.com/hmcts/wa-standalone-task-bpmn.git "${BPMN_BRANCH}"
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
