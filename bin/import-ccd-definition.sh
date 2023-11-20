#!/usr/bin/env bash

set -eu


if ([ $# -eq 0 ] || [ $# -eq 2 ] || ([ $1 != "a" ] && [ $1 != "s" ] && [ $1 != "e" ] && [ $1 != "all" ]))
then
    echo "Usage: ./import-ccd-definition-zsh.sh [e|s|a|all]"
    exit 1
fi
dir=$(dirname ${0})
if [ $1 = "e" ]
then
  echo "*************IMPORTING ENGLANDWALES CONFIG*************"
  if [[ -z "$ENGLANDWALES_CCD_CONFIG_PATH" ]]
  then
    echo "Please set ENGLANDWALES_CCD_CONFIG_PATH environment variable to your local GitHub repo for et-ccd-definitions-englandwales"
    exit 1
  fi
  importFile="${ENGLANDWALES_CCD_CONFIG_PATH}/definitions/xlsx/et-englandwales-ccd-config-cftlib.xlsx"
elif [ $1 = "s" ]
then
  echo "*************IMPORTING SCOTLAND CONFIG*************"
  if [[ -z "$SCOTLAND_CCD_CONFIG_PATH" ]]
  then
    echo "Please set SCOTLAND_CCD_CONFIG_PATH environment variable to your local GiHub repo for et-ccd-definitions-scotland"
    exit 1
  fi
  importFile="${SCOTLAND_CCD_CONFIG_PATH}/definitions/xlsx/et-scotland-ccd-config-cftlib.xlsx"
elif [ $1 = "a" ]
then
  echo "*************IMPORTING ADMIN CONFIG*************"
    if [[ -z "$ADMIN_CCD_CONFIG_PATH" ]]
    then
      echo "Please set ADMIN_CCD_CONFIG_PATH environment variable to your local GiHub repo for et-ccd-definitions-admin"
      exit 1
    fi
    importFile="${ADMIN_CCD_CONFIG_PATH}/definitions/xlsx/et-admin-ccd-config-cftlib.xlsx"
else
  if [ $1 = "all" ]
  then
    echo "*************IMPORTING ALL CONFIGURATIONS*************"
    echo "*************IMPORTING ADMIN CONFIG**********"
    if [[ -z "$ADMIN_CCD_CONFIG_PATH" ]]
    then
      echo "Please set ADMIN_CCD_CONFIG_PATH environment variable to your local GiHub repo for et-ccd-definitions-admin"
    else
      importFile="${ADMIN_CCD_CONFIG_PATH}/definitions/xlsx/et-admin-ccd-config-cftlib.xlsx"
      echo "Using CCD definition file ${importFile}"
      ${dir}/utils/ccd-import-definition.sh ${importFile}
    fi
    echo "***********IMPORTING ENGLANDWALES CONFIG***********"
    if [[ -z "$ENGLANDWALES_CCD_CONFIG_PATH" ]]
    then
      echo "Please set ENGLANDWALES_CCD_CONFIG_PATH environment variable to your local GitHub repo for et-ccd-definitions-englandwales"
    else
      importFile="${ENGLANDWALES_CCD_CONFIG_PATH}/definitions/xlsx/et-englandwales-ccd-config-cftlib.xlsx"
      echo "Using CCD definition file ${importFile}"
      ${dir}/utils/ccd-import-definition.sh ${importFile}
    fi
    echo "**********IMPORTING SCOTLAND CONFIG*************"
      if [[ -z "$SCOTLAND_CCD_CONFIG_PATH" ]]
      then
        echo "Please set SCOTLAND_CCD_CONFIG_PATH environment variable to your local GiHub repo for et-ccd-definitions-scotland"
        exit 1
      else
        importFile="${SCOTLAND_CCD_CONFIG_PATH}/definitions/xlsx/et-scotland-ccd-config-cftlib.xlsx"
        echo "Using CCD definition file ${importFile}"
        ${dir}/utils/ccd-import-definition.sh ${importFile}
      fi
    fi
else
  echo "Importing admin config"
  if [[ -z "$ADMIN_CCD_CONFIG_PATH" ]]
  then
    echo "Please set ADMIN_CCD_CONFIG_PATH environment variable to your local GiHub repo for et-ccd-definitions-admin"
    exit 1
  fi
  importFile="${ADMIN_CCD_CONFIG_PATH}/definitions/xlsx/et-admin-ccd-config-cftlib.xlsx"
fi

if [ $1 != "all" ]
then
  echo "Using CCD definition file ${importFile}"
  ${dir}/utils/ccd-import-definition.sh ${importFile}
fi