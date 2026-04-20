#!/usr/bin/env bash

# Usage: ./add-wa-legal-ops-user.sh <email_id> <region_id> <location_id> <location> <service_code> <user_type> <task_supervisor> <case_allocator> <staff_admin> <suspended> <up_idam_status> <region>
# All arguments are optional and have defaults.

FIRST_NAME="ET"
LAST_NAME="LegalOps"
EMAIL_ID=${1:-"et.caseworker.3@hmcts.net"}
REGION_ID=${2:-12}
LOCATION_ID=${3:-"819890"}
LOCATION=${4:-"Bristol"}
USER_TYPE=${5:-"CTSC"}
TASK_SUPERVISOR=${6:-true}
CASE_ALLOCATOR=${7:-true}
STAFF_ADMIN=${8:-true}
SUSPENDED=${9:-false}
UP_IDAM_STATUS=${10:-"PENDING"}
REGION=${11:-"National"}
SERVICE_CODE=${12:-"BHA1"}

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
response=$(curl -s -w "\n%{http_code}" -X POST "${REF_DATA_URL}/refdata/case-worker/profile" \
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
        {"role_id": "2", "role": "Legal Caseworker", "is_primary": true},
        {"role_id": "1", "role": "Senior Legal Caseworker", "is_primary": true}
      ],
      "skills": [],
      "region": "'"${REGION}"'"
    }'
)
echo "Response received from server. : $response"
http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | head -n-1)
if [ "$http_code" -ge 400 ]; then
  echo "POST failed with status $http_code: $body"
fi
exit 0
