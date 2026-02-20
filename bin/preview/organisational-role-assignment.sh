#!/usr/bin/env bash
## Usage: ./organisational-role-assignment.sh [username] [password] [role_classification] [role_name] [role_attributes] [microservice_name]
##
## Options:
##    - username: Email for user. Default to `ccd-import@fake.hmcts.net`.
##    - password: Password for user. Default to `London01`.
##    - role_classification: Role assignment classification. Default to `PUBLIC`.
##    - role_name: Name of the role for role-assignment. Default to `tribunal-caseworker`.
##    - microservice_name: Name of the microservice to obtain S2S token. Default to `ccd_gw`.
##

USERNAME=${1:-ccd-import@fake.hmcts.net}
PASSWORD=${2:-London01}
ROLE_CLASSIFICATION="${3:-PUBLIC}"
ROLE_NAME="${4:-"tribunal-caseworker"}"
ROLE_ATTRIBUTES="${5:-'{"jurisdiction":"EMPLOYMENT"}'}"

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

curl --silent --show-error -X POST "${ROLE_ASSIGNMENT_URL}/am/role-assignments" \
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
            "roleCategory": "LEGAL_OPERATIONS",
            "readOnly": false,
            "attributes": '${ROLE_ATTRIBUTES}'
          }
        ]
      }'
