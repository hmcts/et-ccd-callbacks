#!/bin/bash
## Usage: ./idam-user-token.sh [user] [password]
##
## Options:
##    - username: Role assigned to user in generated token. Default to `ccd-import@fake.hmcts.net`.
##    - password: ID assigned to user in generated token. Default to `London01`.
##

USERNAME=${1:-ccd-import@fake.hmcts.net}
PASSWORD=${2:-London01}
REDIRECT_URI="https://manage-case.aat.platform.hmcts.net/oauth2/callback"
CLIENT_ID="xuiwebapp"
CLIENT_SECRET="DBssSOq0KKLNBf2z"
SCOPE="profile%20openid%20roles%20manage-user%20create-user%20search-user"

curl --silent --show-error \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -X POST "https://idam-api.aat.platform.hmcts.net/o/token?grant_type=password&redirect_uri=${REDIRECT_URI}&client_id=${CLIENT_ID}&client_secret=${CLIENT_SECRET}&username=${USERNAME}&password=${PASSWORD}&scope=${SCOPE}" -d "" | jq -r .access_token
