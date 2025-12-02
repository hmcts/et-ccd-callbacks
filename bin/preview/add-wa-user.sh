#!/usr/bin/env bash

# Usage: ./add-new-user.sh <first_name> <last_name> <email_id> <region_id> <location_id> <location> <service_code> <user_type> <task_supervisor> <case_allocator> <staff_admin> <suspended> <up_idam_status> <region>
# All arguments are optional and have defaults.

FIRST_NAME=${1:-"WA-User"}
LAST_NAME=${2:-"WA-ET"}
EMAIL_ID=${3:-"et.caseworker.3@hmcts.net"}
REGION_ID=${4:-12}
LOCATION_ID=${5:-"819890"}
LOCATION=${6:-"Bristol"}
USER_TYPE=${7:-"CTSC"}
TASK_SUPERVISOR=${8:-true}
CASE_ALLOCATOR=${9:-true}
STAFF_ADMIN=${10:-true}
SUSPENDED=${11:-false}
UP_IDAM_STATUS=${12:-"PENDING"}
REGION=${13:-"National"}

# Get the directory of this script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Use preview-specific token utilities
source "${SCRIPT_DIR}/utils/auth-utils.sh"

echo "🔐 Getting authentication tokens..."
echo "Retrieving IDAM user token"
USER_TOKEN=$(get_staff_admin_token)
echo "Retrieving S2S service token for xui_webapp"
SERVICE_TOKEN=$(get_service_token "xui_webapp")

# Function to check if user exists by name/email/service
check_user_exists() {
  local search_term="$1"
  local email_to_check="$2"
  local service_to_check="$3"
  local search_url="${REF_DATA_URL}/refdata/case-worker/profile/search-by-name?search=${search_term}"

  local result=$(curl -s -X GET "$search_url" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer ${USER_TOKEN}" \
    -H "ServiceAuthorization: Bearer ${SERVICE_TOKEN}")

  local found=$(echo "$result" | jq -e --arg email "$email_to_check" --arg service "$service_to_check" '
    if type == "array" then . else [.] end
    | map(select(.email_id == $email and (.services // [] | map(.service == $service) | any)))
    | length > 0')

  if [ "$found" = "true" ]; then
    echo "User with name/email/service $search_term/$email_to_check/$service_to_check already exists. Skipping creation."
    return 0
  fi
  return 1
}

# Check for existing user before creation (by first name/email/service)
if check_user_exists "$FIRST_NAME" "$EMAIL_ID" "Employment Claims"; then
  echo "User already exists. Exiting."
  exit 0
fi

echo "Creating user ${FIRST_NAME} ${LAST_NAME} with email ${EMAIL_ID}"
curl -v --silent --show-error -X POST "${REF_DATA_URL}/refdata/case-worker/profile" \
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
      "service_codes": ["BHA1"]
    }],
    "user_type": "'"${USER_TYPE}"'",
    "task_supervisor": '"${TASK_SUPERVISOR}"',
    "case_allocator": '"${CASE_ALLOCATOR}"',
    "staff_admin": '"${STAFF_ADMIN}"',
    "suspended": '"${SUSPENDED}"',
    "up_idam_status": "'"${UP_IDAM_STATUS}"'",
    "services": [{
      "service": "Employment Claims",
      "service_code": "BHA1"
    }],
    "roles": [
      {"role_id": "10", "role": "CTSC Administrator", "is_primary": true},
      {"role_id": "9", "role": "CTSC Team Leader", "is_primary": true},
      {"role_id": "4", "role": "Hearing Centre Administrator", "is_primary": true},
      {"role_id": "3", "role": "Hearing Centre Team Leader", "is_primary": true},
      {"role_id": "2", "role": "Legal Caseworker", "is_primary": true},
      {"role_id": "13", "role": "Regional Centre Administrator", "is_primary": true},
      {"role_id": "12", "role": "Regional Centre Team Leader", "is_primary": true},
      {"role_id": "1", "role": "Senior Legal Caseworker", "is_primary": true}
    ],
    "skills": [],
    "region": "'"${REGION}"'"
  }'
