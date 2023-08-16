#!/bin/bash
## Usage: ./idam-user-token.sh [user] [password]
##
## Options:
##    - username: Role assigned to user in generated token. Default to `ccd-import@fake.hmcts.net`.
##    - password: ID assigned to user in generated token. Default to `London01`.
##
## Returns a valid IDAM user token for the given username and password.

USERNAME=${CCD_IMPORT_USERNAME:-"servicesatcdm+ethos@gmail.com"}
PASSWORD=I${CCD_IMPORT_PASSWORD:-"Adventure2019"}
REDIRECT_URI="https://et-sya.aat.platform.hmcts.net/oauth2/callback"
CLIENT_ID="et-sya"
CLIENT_SECRET=${IDAM_CLIENT_SECRET:-"ZSu8eMK9Woqc0Tm9"}
SCOPE="openid%20profile%20roles"

curl --silent --show-error \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -XPOST "${IDAM_API_BASE_URL}/o/token?grant_type=password&redirect_uri=${REDIRECT_URI}&client_id=${CLIENT_ID}&client_secret=${CLIENT_SECRET}&username=${USERNAME}&password=${PASSWORD}&scope=${SCOPE}" -d "" | jq -r .access_token