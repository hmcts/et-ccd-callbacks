#!/usr/bin/env bash

echo "Adding CTSC User"
./bin/preview/add-wa-user.sh "CTSC" "User" "${ET_CASEOFFICER_USERNAME}" 12 "819890" "Bristol" "BHA1" "CTSC" "true" "true" "true" "false" "PENDING" "National"
echo "Adding LegalOps User"
./bin/preview/add-wa-user.sh "LegalOps" "User" "${ET_CCD_CASEWORKER_USER_NAME}" 12 "819890" "Bristol" "BHA1" "Legal office" "true" "true" "true" "false" "PENDING" "National"