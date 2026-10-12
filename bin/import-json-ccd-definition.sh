#!/usr/bin/env bash

set -eu

if [[ $# -ne 1 ]] || [[ ! "$1" =~ ^(e|s|a|all)$ ]]; then
  echo "Usage: ./bin/import-json-ccd-definition.sh [e|s|a|all]"
  exit 1
fi

script_dir=$(cd "$(dirname "$0")" && pwd)
project_path=${CALLBACKS_PROJECT_PATH:-$(cd "${script_dir}/.." && pwd)}

import_jurisdiction() {
  local jurisdiction=$1
  local definition_root="${project_path}/ccd-definitions/jurisdictions/${jurisdiction}"

  echo "Importing ${jurisdiction} JSON CCD definition"
  "${script_dir}/utils/ccd-import-json-definition.sh" \
    "${definition_root}/json" \
    "${definition_root}/data/ccd-template.xlsx"
}

case "$1" in
  e)
    import_jurisdiction england-wales
    ;;
  s)
    import_jurisdiction scotland
    ;;
  a)
    import_jurisdiction admin
    ;;
  all)
    import_jurisdiction admin
    import_jurisdiction england-wales
    import_jurisdiction scotland
    ;;
esac
