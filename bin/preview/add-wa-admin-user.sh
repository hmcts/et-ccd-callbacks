#!/usr/bin/env bash

# Usage: ./add-wa-admin-user.sh <region_id> <location_id> <location> <service_code> <user_type> <task_supervisor> <case_allocator> <staff_admin> <suspended> <up_idam_status> <region>
# All arguments are optional and have defaults.

FIRST_NAME="ET"
LAST_NAME="Admin"
EMAIL_ID="${ET_CASEWORKER_USER_NAME}"
REGION_ID=${1:-12}
LOCATION_ID=${2:-"36313"}
LOCATION=${3:-"Leeds"}
USER_TYPE=${4:-"CTSC"}
TASK_SUPERVISOR=${5:-true}
CASE_ALLOCATOR=${6:-true}
STAFF_ADMIN=${7:-true}
SUSPENDED=${8:-false}
UP_IDAM_STATUS=${9:-"PENDING"}
REGION=${10:-"National"}
SERVICE_CODE=${11:-"BHA1"}
CURL_CONNECT_TIMEOUT_SECONDS=30
CURL_MAX_TIME_SECONDS=120
CURL_RETRY_COUNT=3
CURL_RETRY_DELAY_SECONDS=5

# Get the directory of this script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Use preview-specific token utilities
source "${SCRIPT_DIR}/utils/auth-utils.sh"

echo "🔐 Getting authentication tokens..."
echo "Retrieving IDAM user token"
USER_TOKEN=$(get_staff_admin_token)
echo "Retrieving S2S service token for xui_webapp"
SERVICE_TOKEN=$(get_service_token "xui_webapp")

echo "Creating user ${FIRST_NAME} ${LAST_NAME} with email ${EMAIL_ID}"
echo "Using HTTP/1.1 with ${CURL_RETRY_COUNT} retries, ${CURL_CONNECT_TIMEOUT_SECONDS}s connect timeout, ${CURL_MAX_TIME_SECONDS}s max time"
response_body_file=$(mktemp)
if http_code=$(curl --silent --show-error --location \
  --http1.1 \
  --retry "${CURL_RETRY_COUNT}" \
  --retry-delay "${CURL_RETRY_DELAY_SECONDS}" \
  --retry-all-errors \
  --connect-timeout "${CURL_CONNECT_TIMEOUT_SECONDS}" \
  --max-time "${CURL_MAX_TIME_SECONDS}" \
  --output "${response_body_file}" \
  --write-out "%{http_code}" \
  -X POST "${REF_DATA_URL}/refdata/case-worker/profile" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${USER_TOKEN}" \
  -H "ServiceAuthorization: Bearer ${SERVICE_TOKEN}" \
  -d '{
      "first_name": "'"${FIRST_NAME}"'",
      "last_name": "'"${LAST_NAME}"'",
      "email_id": "'"${EMAIL_ID}"'",
      "region_id": '"${REGION_ID}"',
      "base_locations": [{
        "location_id": "'"${LOCATION_ID}"'",
        "location": "'"${LOCATION}"'",
        "is_primary": true,
        "service_codes": ["'"${SERVICE_CODE}"'"]
      }],
      "user_type": "'"${USER_TYPE}"'",
      "task_supervisor": '"${TASK_SUPERVISOR}"',
      "case_allocator": '"${CASE_ALLOCATOR}"',
      "staff_admin": '"${STAFF_ADMIN}"',
      "suspended": '"${SUSPENDED}"',
      "up_idam_status": "'"${UP_IDAM_STATUS}"'",
      "services": [{
        "service": "Employment Claims",
        "service_code": "'"${SERVICE_CODE}"'"
      }],
      "roles": [
        {"role_id": "10", "role": "CTSC Administrator", "is_primary": true},
        {"role_id": "9", "role": "CTSC Team Leader", "is_primary": true},
        {"role_id": "4", "role": "Hearing Centre Administrator", "is_primary": true},
        {"role_id": "3", "role": "Hearing Centre Team Leader", "is_primary": true},
        {"role_id": "13", "role": "Regional Centre Administrator", "is_primary": true},
        {"role_id": "12", "role": "Regional Centre Team Leader", "is_primary": true}
      ],
      "skills": [],
      "region": "'"${REGION}"'"
    }'
); then
  :
else
  curl_exit_code="$?"
  echo "POST failed due to curl error ${curl_exit_code}"
  if [[ -s "${response_body_file}" ]]; then
    echo "Response: $(cat "${response_body_file}")"
  fi
  rm -f "${response_body_file}"
  exit "${curl_exit_code}"
fi

body=$(cat "${response_body_file}")
rm -f "${response_body_file}"
echo "Response received from server. : ${body}"
echo "${http_code}"
if [[ "${http_code}" =~ ^[0-9]+$ ]] && [ "$http_code" -ge 400 ]; then
  echo "POST failed with status $http_code: $body"
fi
exit 0
