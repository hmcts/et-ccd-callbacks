#!/bin/bash
## Usage: ./idam-user-token.sh [user] [password]
##
## Options:
##    - username: Role assigned to user in generated token. Default to `ccd-import@fake.hmcts.net`.
##    - password: ID assigned to user in generated token. Default to `London01`.
##
## Returns a valid IDAM user token for the given username and password.

USERNAME=${CCD_IMPORT_USERNAME}
PASSWORD=${CCD_IMPORT_PASSWORD}

curl --location "${IDAM_API_BASE_URL}"/loginUser \
--header "Content-Type: application/x-www-form-urlencoded" \
--data-urlencode "username=""${USERNAME}" \
--data-urlencode "password=""${PASSWORD}"| jq -r .access_token