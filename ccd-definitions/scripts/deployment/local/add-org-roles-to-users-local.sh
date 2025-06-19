#!/usr/bin/env bash

USER_NAME="et.casectsc@hmcts.net"
PASSWORD="System01"
# Setup Users
./create-user.sh ${USER_NAME} "ET" "Admin" ${PASSWORD} "caseworker" "[{ \"code\": \"caseworker\"}, { \"code\": \"caseworker-employment\"}, { \"code\": \"caseworker-employment-api\"}, { \"code\": \"caseworker-employment-englandwales\"}, { \"code\": \"caseworker-wa\"}, { \"code\": \"caseworker-wa-task-configuration\"}]"

# Add Roles
echo ""
echo "Setting up WA Users and role assignments..."
./organisational-role-assignment-local.sh ${USER_NAME} ${PASSWORD} "PUBLIC" "case-allocator" '{"jurisdiction":"EMPLOYMENT","primaryLocation":"765324"}'
./organisational-role-assignment-local.sh ${USER_NAME} ${PASSWORD} "PUBLIC" "ctsc-team-leader" '{"jurisdiction":"EMPLOYMENT","primaryLocation":"765324"}'
