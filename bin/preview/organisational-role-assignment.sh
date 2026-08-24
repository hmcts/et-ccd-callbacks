#!/usr/bin/env bash

set -euo pipefail
## Usage: ./organisational-role-assignment.sh [username] [password] [role_classification] [role_name] [role_attributes] [role_category]
##
## Options:
##    - username: Email for user. Default to `ccd-import@fake.hmcts.net`.
##    - password: Password for user. Default to `London01`.
##    - role_classification: Role assignment classification. Default to `PUBLIC`.
##    - role_name: Name of the role for role-assignment. Default to `tribunal-caseworker`.
##    - role_attributes: JSON attributes for the role. Default to `{"jurisdiction":"EMPLOYMENT"}`.
##    - role_category: Role category. Default to `LEGAL_OPERATIONS`.
##

USERNAME=${1:-ccd-import@fake.hmcts.net}
PASSWORD=${2:-London01}
ROLE_CLASSIFICATION="${3:-PUBLIC}"
ROLE_NAME="${4:-"tribunal-caseworker"}"
ROLE_ATTRIBUTES="${5:-'{"jurisdiction":"EMPLOYMENT"}'}"
ROLE_CATEGORY="${6:-LEGAL_OPERATIONS}"
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
USER_TOKEN=$(get_user_token_from_email_password "$USERNAME" "$PASSWORD")
echo "Retrieving user ID from token"
USER_ID=$(get_idam_id_from_token "$USER_TOKEN")
echo "Retrieving S2S service token for xui_webapp"
SERVICE_TOKEN=$(get_service_token "xui_webapp")

echo "\n\nCreating role assignment: \n User: ${USER_ID}\n Role name: ${ROLE_NAME}\n ROLE_CLASSIFICATION: ${ROLE_CLASSIFICATION}\n"

response_body_file=$(mktemp)
trap 'rm -f "${response_body_file}"' EXIT
if http_code=$(curl --silent --show-error --location \
  --http1.1 \
  --fail-with-body \
  --retry "${CURL_RETRY_COUNT}" \
  --retry-delay "${CURL_RETRY_DELAY_SECONDS}" \
  --retry-all-errors \
  --connect-timeout "${CURL_CONNECT_TIMEOUT_SECONDS}" \
  --max-time "${CURL_MAX_TIME_SECONDS}" \
  --output "${response_body_file}" \
  --write-out "%{http_code}" \
  -X POST "${ROLE_ASSIGNMENT_URL}/am/role-assignments" \
  -H "accept: application/vnd.uk.gov.hmcts.role-assignment-service.create-assignments+json;charset=UTF-8;version=1.0" \
  -H "Authorization: Bearer ${USER_TOKEN}" \
  -H "ServiceAuthorization: Bearer ${SERVICE_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{ "roleRequest": {
          "assignerId": "'"${USER_ID}"'",
          "process": "staff-organisational-role-mapping",
          "reference": "'"${USER_ID}/${ROLE_NAME}"'",
          "replaceExisting": true
        },
        "requestedRoles": [
          {
            "actorIdType": "IDAM",
            "actorId": "'"${USER_ID}"'",
            "roleType": "ORGANISATION",
            "roleName": "'"${ROLE_NAME}"'",
            "classification": "'"${ROLE_CLASSIFICATION}"'",
            "grantType": "STANDARD",
            "roleCategory": "'"${ROLE_CATEGORY}"'",
            "readOnly": false,
            "attributes": '"${ROLE_ATTRIBUTES}"'
          }
        ]
      }'); then
  curl_exit_code=0
else
  curl_exit_code="$?"
fi

body=$(cat "${response_body_file}")
echo "Response received from server. : ${body}"
echo "${http_code}"

if (( curl_exit_code != 0 )); then
  echo "POST failed due to curl error ${curl_exit_code} (HTTP ${http_code}): ${body}"
  exit "${curl_exit_code}"
fi

if [[ ! "${http_code}" =~ ^2[0-9][0-9]$ ]]; then
  echo "POST failed with unexpected status ${http_code}: ${body}"
  exit 1
fi
