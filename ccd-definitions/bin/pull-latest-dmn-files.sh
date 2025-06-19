#!/usr/bin/env bash

branchName=$1

#Checkout specific branch pf  camunda bpmn definition
git clone https://github.com/hmcts/et-wa-task-configuration.git
cd et-wa-task-configuration

echo "Switch to ${branchName} branch on et-wa-task-configuration"
git checkout ${branchName}
cd ..

#Copy camunda folder which contains dmn files
cp -r ./et-wa-task-configuration/src/main/resources .
rm -rf ./et-wa-task-configuration

./bin/import-dmn-diagram.sh . employment et
