#!/usr/bin/env bash

set -eu

if ([ $# -eq 0 ] || [ $# -gt 1 ] || ([ "$1" != "a" ] && [ "$1" != "s" ] && [ "$1" != "e" ] && [ "$1" != "all" ])); then
  echo "Usage: ./import-ccd-definition.sh [e|s|a|all]"
  exit 1
fi

script_dir=$(cd -- "$(dirname -- "$0")" && pwd)
repo_root=$(cd -- "${script_dir}/.." && pwd)
definitions_root="${repo_root}/ccd-definitions/dist/cftlib"

admin_file="${definitions_root}/et-admin-ccd-config-cftlib.xlsx"
england_wales_file="${definitions_root}/et-englandwales-ccd-config-cftlib.xlsx"
scotland_file="${definitions_root}/et-scotland-ccd-config-cftlib.xlsx"

ensure_file_exists() {
  local file="$1"
  if [[ ! -f "${file}" ]]; then
    echo "Missing CCD definition file: ${file}"
    echo "Generate local cftlib files with:"
    echo "  cd ${repo_root}/ccd-definitions && yarn setup && yarn generate-excel:cftlib"
    exit 1
  fi
}

import_file() {
  local file="$1"
  ensure_file_exists "${file}"
  echo "Using CCD definition file ${file}"
  "${script_dir}/utils/ccd-import-definition.sh" "${file}"
}

case "$1" in
  a)
    echo "*************IMPORTING ADMIN CONFIG*************"
    import_file "${admin_file}"
    ;;
  e)
    echo "*************IMPORTING ENGLANDWALES CONFIG*************"
    import_file "${england_wales_file}"
    ;;
  s)
    echo "*************IMPORTING SCOTLAND CONFIG*************"
    import_file "${scotland_file}"
    ;;
  all)
    echo "*************IMPORTING ALL CONFIGURATIONS*************"
    echo "*************IMPORTING ADMIN CONFIG*************"
    import_file "${admin_file}"
    echo "*************IMPORTING ENGLANDWALES CONFIG*************"
    import_file "${england_wales_file}"
    echo "*************IMPORTING SCOTLAND CONFIG*************"
    import_file "${scotland_file}"
    ;;
esac
