#!/usr/bin/env bash

set -euo pipefail

usage() {
  cat <<'USAGE'
Usage:
  bin/preview/retention-preview-test.sh <pr-number> <seed|list|expire|simulate|delete|cleanup>

Environment overrides:
  RETENTION_PREVIEW_URL       Full et-cos preview URL. Defaults to https://et-cos-pr-<pr>.preview.platform.hmcts.net
  RETENTION_PREVIEW_TOKEN     Preview endpoint token. Defaults to <pr-number>.
  RUN_ID                      Test run id. Defaults to retention-preview-<pr-number>.
  CASE_TYPE_ID                Seeded case type. Defaults to ET_EnglandWales.
  CASE_TYPE_IDS               Comma-separated case types for run. Defaults to CASE_TYPE_ID.
  EXPIRED_COUNT               Seeded expired draft cases. Defaults to 2.
  FUTURE_COUNT                Seeded future draft cases. Defaults to 1.
  DAYS_IN_PAST                Expire TTL by this many days. Defaults to 1.
  REFERENCES                  Comma-separated case references to expire. Defaults to all rows for RUN_ID.
  BATCH_SIZE                  Retention run batch size. Defaults to 25.
  INCLUDE_NON_PREVIEW_CASES   Set true to allow processing untagged expired rows. Defaults to false.

Examples:
  bin/preview/retention-preview-test.sh 1234 seed
  bin/preview/retention-preview-test.sh 1234 simulate
  bin/preview/retention-preview-test.sh 1234 delete
  bin/preview/retention-preview-test.sh 1234 cleanup
USAGE
}

if [[ $# -ne 2 ]]; then
  usage
  exit 1
fi

pr_number="$1"
command="$2"
base_url="${RETENTION_PREVIEW_URL:-https://et-cos-pr-${pr_number}.preview.platform.hmcts.net}"
token="${RETENTION_PREVIEW_TOKEN:-${pr_number}}"
run_id="${RUN_ID:-retention-preview-${pr_number}}"
case_type_id="${CASE_TYPE_ID:-ET_EnglandWales}"
case_type_ids="${CASE_TYPE_IDS:-${case_type_id}}"
batch_size="${BATCH_SIZE:-25}"

request() {
  local method="$1"
  local path="$2"
  local payload="${3:-}"

  if [[ -n "${payload}" ]]; then
    curl --fail --silent --show-error --location \
      --request "${method}" \
      "${base_url}${path}" \
      --header "Content-Type: application/json" \
      --header "X-Retention-Test-Token: ${token}" \
      --data "${payload}" | jq .
  else
    curl --fail --silent --show-error --location \
      --request "${method}" \
      "${base_url}${path}" \
      --header "X-Retention-Test-Token: ${token}" | jq .
  fi
}

references_json() {
  if [[ -z "${REFERENCES:-}" ]]; then
    jq --compact-output --null-input '[]'
    return
  fi

  jq --compact-output --null-input --arg references "${REFERENCES}" \
    '$references | split(",") | map(gsub("^\\s+|\\s+$"; "")) | map(select(length > 0)) | map(tonumber)'
}

case_types_json() {
  jq --compact-output --null-input --arg caseTypeIds "${case_type_ids}" \
    '$caseTypeIds | split(",") | map(gsub("^\\s+|\\s+$"; "")) | map(select(length > 0))'
}

case "${command}" in
  seed)
    payload=$(jq --null-input \
      --arg runId "${run_id}" \
      --arg caseTypeId "${case_type_id}" \
      --arg jurisdiction "${JURISDICTION:-EMPLOYMENT}" \
      --arg state "${STATE:-AWAITING_SUBMISSION_TO_HMCTS}" \
      --argjson expiredCount "${EXPIRED_COUNT:-2}" \
      --argjson futureCount "${FUTURE_COUNT:-1}" \
      '{
        runId: $runId,
        caseTypeId: $caseTypeId,
        jurisdiction: $jurisdiction,
        state: $state,
        expiredCount: $expiredCount,
        futureCount: $futureCount
      }')
    request POST /testing/retention/seed "${payload}"
    ;;
  list)
    request GET "/testing/retention/cases?runId=${run_id}"
    ;;
  expire)
    payload=$(jq --null-input \
      --arg runId "${run_id}" \
      --argjson references "$(references_json)" \
      --argjson daysInPast "${DAYS_IN_PAST:-1}" \
      '{runId: $runId, references: $references, daysInPast: $daysInPast}')
    request POST /testing/retention/expire "${payload}"
    ;;
  simulate)
    payload=$(jq --null-input \
      --argjson caseTypeIds "$(case_types_json)" \
      --argjson batchSize "${batch_size}" \
      --argjson includeNonPreviewCases "${INCLUDE_NON_PREVIEW_CASES:-false}" \
      '{
        mode: "simulation",
        caseTypeIds: $caseTypeIds,
        batchSize: $batchSize,
        includeNonPreviewCases: $includeNonPreviewCases
      }')
    request POST /testing/retention/run "${payload}"
    ;;
  delete)
    payload=$(jq --null-input \
      --argjson caseTypeIds "$(case_types_json)" \
      --argjson batchSize "${batch_size}" \
      --argjson includeNonPreviewCases "${INCLUDE_NON_PREVIEW_CASES:-false}" \
      '{
        mode: "deletion",
        caseTypeIds: $caseTypeIds,
        batchSize: $batchSize,
        includeNonPreviewCases: $includeNonPreviewCases
      }')
    request POST /testing/retention/run "${payload}"
    ;;
  cleanup)
    request DELETE "/testing/retention/cases?runId=${run_id}"
    ;;
  *)
    usage
    exit 1
    ;;
esac
