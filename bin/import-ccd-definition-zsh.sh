#!/usr/bin/env bash

set -eu

if [ -z "$1" ]
then
    echo "Usage: ./import-ccd-config.sh [e|s|a]"
    exit 1
fi

if [ $1 = "e" ]
then
  echo "Import EnglandWales config"
  if [[ -z "$ENGLANDWALES_CCD_CONFIG_PATH" ]]
  then
    echo "Please set ENGLANDWALES_CCD_CONFIG_PATH environment variable to your local GitHub repo for et-ccd-definitions-englandwales"
    exit 1
  fi
  importFile="${ENGLANDWALES_CCD_CONFIG_PATH}/definitions/xlsx/et-englandwales-ccd-config-cftlib.xlsx"
elif [ $1 = "s" ]
then
  echo "Import Scotland config"
  if [[ -z "$SCOTLAND_CCD_CONFIG_PATH" ]]
  then
    echo "Please set SCOTLAND_CCD_CONFIG_PATH environment variable to your local GiHub repo for et-ccd-definitions-scotland"
    exit 1
  fi
  importFile="${SCOTLAND_CCD_CONFIG_PATH}/definitions/xlsx/et-scotland-ccd-config-cftlib.xlsx"
else
  echo "Importing admin config"
  if [[ -z "$ADMIN_CCD_CONFIG_PATH" ]]
  then
    echo "Please set ADMIN_CCD_CONFIG_PATH environment variable to your Local GiHub repo for et-ccd-definitions-admin"
    exit 1
  fi
  importFile="${ADMIN_CCD_CONFIG_PATH}/definitions/xlsx/et-admin-ccd-config-cftlib.xlsx"
fi

echo "Using CCD definition file ${importFile}"

dir=$(dirname ${0})

${dir}/utils/ccd-import-definition.sh ${importFile}
