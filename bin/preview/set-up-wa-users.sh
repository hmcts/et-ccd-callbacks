#!/usr/bin/env bash

echo "Adding CTSC User"
./bin/preview/add-wa-user.sh "${ET_STAFF_USER_ADMIN_USER_NAME}" "${ET_STAFF_USER_ADMIN_USER_NAME_PASSWORD}" "CTSC" "User" "${ET_CASEWORKER_USER_NAME}"
echo "Adding LegalOps User"
./bin/preview/add-wa-user.sh "${ET_STAFF_USER_ADMIN_USER_NAME}" "${ET_STAFF_USER_ADMIN_USER_NAME_PASSWORD}" "LegalOps" "User" "${ET_CCD_CASEWORKER_USER_NAME}" "" "" "" "" "Legal office"