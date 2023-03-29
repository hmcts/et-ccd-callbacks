#!/bin/sh

IMPORTER_USERNAME="ccd.docker.default@hmcts.net"
IMPORTER_PASSWORD="Pa55word11"
IDAM_URI="http://localhost:5000"
REDIRECT_URI="http://localhost:3451/oauth2redirect"
CLIENT_ID="ccd_gateway"
CLIENT_SECRET="ccd_gateway_secret"
CURL_OPTS="$CURL_OPTS -S --silent"

code=$(curl ${CURL_OPTS} -u "${IMPORTER_USERNAME}:${IMPORTER_PASSWORD}" -XPOST "${IDAM_URI}/oauth2/authorize?redirect_uri=${REDIRECT_URI}&response_type=code&client_id=${CLIENT_ID}" -d "" | jq -r .code)

curl ${CURL_OPTS} -H "Content-Type: application/x-www-form-urlencoded" -u "${CLIENT_ID}:${CLIENT_SECRET}" -XPOST "${IDAM_URI}/oauth2/token?code=${code}&redirect_uri=${REDIRECT_URI}&grant_type=authorization_code" -d "" | jq -r .access_token
