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


USER_TOKEN=$(./idam-user-token.sh $USERNAME $PASSWORD)
echo "user token ${USER_TOKEN}"
USER_ID=$(./idam-user-id.sh $USER_TOKEN)
echo "user id ${USER_ID}"

SERVICE_TOKEN=$(./idam-lease-service-token.sh et_cos \
$(docker run --rm toolbelt/oathtool --totp -b MW4J273C5XMEROVI))

echo "service token ${SERVICE_TOKEN}"


echo "\n\nCreating role assignment: \n User: ${USER_ID}\n Role name: ${ROLE_NAME}\n ROLE_CLASSIFICATION: ${ROLE_CLASSIFICATION}\n"

curl --silent --show-error -X POST https://am-role-assignment-et-ccd-definitions-admin-pr-113.preview.platform.hmcts.net/am/role-assignments \
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
