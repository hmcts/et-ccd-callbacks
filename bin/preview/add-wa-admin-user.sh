#!/usr/bin/env bash

# Usage: ./add-wa-admin-user.sh <region_id> <location_id> <location> <service_code> <user_type> <task_supervisor> <case_allocator> <staff_admin> <suspended> <up_idam_status> <region>
# All arguments are optional and have defaults.

FIRST_NAME="ET"
LAST_NAME="Admin"
EMAIL_ID="${ET_CASEWORKER_USER_NAME}"
REGION_ID=${1:-12}
LOCATION_ID=${2:-"819890"}
LOCATION=${3:-"Bristol"}
USER_TYPE=${4:-"CTSC"}
TASK_SUPERVISOR=${5:-true}
CASE_ALLOCATOR=${6:-true}
STAFF_ADMIN=${7:-true}
SUSPENDED=${8:-false}
UP_IDAM_STATUS=${9:-"PENDING"}
REGION=${10:-"National"}
SERVICE_CODE=${11:-"BHA1"}

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
)
echo "Response received from server. : $response"
http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | head -n-1)
if [ "$http_code" -ge 400 ]; then
  echo "POST failed with status $http_code: $body"
fi
exit 0
