#!/usr/bin/env bash

# Setup Users
echo ""
echo "Setting up WA Users and role assignments..."
echo "Setting up LEGAL_OPERATIONS"
./bin/preview/organisational-role-assignment.sh "${ET_CASEOFFICER_USERNAME}" "${ET_CASEOFFICER_PASSWORD}" "PUBLIC" "case-allocator" '{"jurisdiction":"EMPLOYMENT","primaryLocation":"765324"}' "LEGAL_OPERATIONS"
./bin/preview/organisational-role-assignment.sh "${ET_CASEOFFICER_USERNAME}" "${ET_CASEOFFICER_PASSWORD}" "PUBLIC" "task-supervisor" '{"jurisdiction":"EMPLOYMENT","primaryLocation":"765324"}' "LEGAL_OPERATIONS"
./bin/preview/organisational-role-assignment.sh "${ET_CASEOFFICER_USERNAME}" "${ET_CASEOFFICER_PASSWORD}" "PUBLIC" "tribunal-caseworker" '{"jurisdiction":"EMPLOYMENT","primaryLocation":"765324"}' "LEGAL_OPERATIONS"

./bin/preview/add-wa-legal-ops-user.sh "${ET_CASEOFFICER_USERNAME}"

echo "Setting up ADMIN"
./bin/preview/organisational-role-assignment.sh "${ET_COS_SYSTEM_USER}" "${ET_COS_SYSTEM_USER_PASSWORD}" "PUBLIC" "case-allocator" '{"jurisdiction":"EMPLOYMENT","primaryLocation":"765324"}' "ADMIN"
./bin/preview/organisational-role-assignment.sh "${ET_COS_SYSTEM_USER}" "${ET_COS_SYSTEM_USER_PASSWORD}" "PUBLIC" "task-supervisor" '{"jurisdiction":"EMPLOYMENT","primaryLocation":"765324"}' "ADMIN"
./bin/preview/organisational-role-assignment.sh "${ET_COS_SYSTEM_USER}" "${ET_COS_SYSTEM_USER_PASSWORD}" "PUBLIC" "tribunal-caseworker" '{"jurisdiction":"EMPLOYMENT","primaryLocation":"765324"}' "ADMIN"
./bin/preview/add-wa-admin-user.sh "${ET_COS_SYSTEM_USER}"