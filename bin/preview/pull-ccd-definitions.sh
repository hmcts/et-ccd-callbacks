#!/usr/bin/env bash

if [ -z "$1" ]; then
    branchName=master
else
  branchName=$1
fi

mkdir "definitions"

# EnglandWales Config Files
git clone https://github.com/hmcts/et-ccd-definitions-englandwales.git
cd et-ccd-definitions-englandwales

echo "Switch to ${branchName} branch on et-ccd-definition-englandwales"
git checkout "${branchName}"
yarn install
yarn generate-excel-preview "$2"
cp ./definitions/xlsx/et-englandwales-ccd-config-preview.xlsx ../definitions/et-englandwales-ccd-config-preview.xlsx
cd ..

# Scotland Config Files
git clone https://github.com/hmcts/et-ccd-definitions-scotland.git
cd et-ccd-definitions-scotland

echo "Switch to ${branchName} branch on et-ccd-definition-scotland"
git checkout ${branchName}
yarn install
yarn generate-excel-preview "$2"
cp ./definitions/xlsx/et-scotland-ccd-config-preview.xlsx ../definitions/et-scotland-ccd-config-preview.xlsx
cd ..

# Cleanup
rm -rf et-ccd-definitions-scotland && rm -rf et-ccd-definitions-englandwales