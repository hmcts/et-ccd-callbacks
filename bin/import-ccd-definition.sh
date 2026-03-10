#!/usr/bin/env bash

set -eu


if ([ $# -eq 0 ] || [ $# -eq 2 ] || ([ $1 != "a" ] && [ $1 != "s" ] && [ $1 != "e" ] && [ $1 != "all" ]))
then

    echo "Usage: ./import-ccd-definition-zsh.sh [e|s|a|all]"
    exit 1
fi
dir=$(dirname ${0})

if [[ -z "$CALLBACKS_PROJECT_PATH" ]]
  then
    echo "Please set CALLBACKS_PROJECT_PATH variable to your local environment"
    exit 1
fi

if [ $1 = "e" ]
then
  echo "*************IMPORTING ENGLANDWALES CONFIG*************"
  importFile="${CALLBACKS_PROJECT_PATH}/ccd-definitions/jurisdictions/england-wales/xlsx/et-englandwales-ccd-config-cftlib.xlsx"
elif [ $1 = "s" ]
then
  echo "*************IMPORTING SCOTLAND CONFIG*************"
  importFile="${CALLBACKS_PROJECT_PATH}/ccd-definitions/jurisdictions/scotland/xlsx/et-scotland-ccd-config-cftlib.xlsx"
elif [ $1 = "a" ]
then
  echo "*************IMPORTING ADMIN CONFIG*************"
  importFile="${CALLBACKS_PROJECT_PATH}/ccd-definitions/jurisdictions/admin/xlsx/et-admin-ccd-config-cftlib.xlsx"
else
  if [ $1 = "all" ]
  then
    echo "*************IMPORTING ALL CONFIGURATIONS*************"
    echo "*************IMPORTING ADMIN CONFIG**********"
    importFile="${CALLBACKS_PROJECT_PATH}/ccd-definitions/jurisdictions/admin/xlsx/et-admin-ccd-config-cftlib.xlsx"
    echo "Using CCD definition file ${importFile}"
    ${dir}/utils/ccd-import-definition.sh ${importFile}

    echo "***********IMPORTING ENGLANDWALES CONFIG***********"
    importFile="${CALLBACKS_PROJECT_PATH}/ccd-definitions/jurisdictions/england-wales/xlsx/et-englandwales-ccd-config-cftlib.xlsx"
    echo "Using CCD definition file ${importFile}"
    ${dir}/utils/ccd-import-definition.sh ${importFile}

    echo "**********IMPORTING SCOTLAND CONFIG*************"
    importFile="${CALLBACKS_PROJECT_PATH}/ccd-definitions/jurisdictions/scotland/xlsx/et-scotland-ccd-config-cftlib.xlsx"
    echo "Using CCD definition file ${importFile}"
    ${dir}/utils/ccd-import-definition.sh ${importFile}
    fi
fi
if [ $1 != "all" ]
then
  echo "Using CCD definition file ${importFile}"
  ${dir}/utils/ccd-import-definition.sh ${importFile}
fi