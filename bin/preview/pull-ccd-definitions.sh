#!/usr/bin/env bash

if [ -z "$1" ]; then
    branchName=master
else
  branchName=$1
fi

# EnglandWales Config Files
git clone https://github.com/hmcts/et-ccd-definitions-englandwales.git
cd et-ccd-definitions-englandwales

echo "Switch to ${branchName} branch on et-ccd-definition-englandwales"
git checkout ${branchName}
yarn install
yarn generate-excel-aat
cp ./definitions/xlsx/et-englandwales-ccd-config-aat.xlsx .
cd..

# Scotland Config Files
git clone https://github.com/hmcts/et-ccd-definitions-scotland.git
cd et-ccd-definitions-scotland

echo "Switch to ${branchName} branch on et-ccd-definition-scotland"
git checkout ${branchName}
yarn install
yarn generate-excel-aat
cp ./definitions/xlsx/et-scotland-ccd-config-aat.xlsx .
