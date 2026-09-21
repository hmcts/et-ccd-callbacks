#!/usr/bin/env bash

set -eu

if [[ $# -ne 2 ]]; then
  echo "Usage: ./ccd-import-json-definition.sh path_to_json_directory path_to_template"
  exit 1
fi

if [[ ! -d "$1" ]]; then
  echo "Directory not found: $1"
  exit 1
fi

if [[ ! -f "$2" ]]; then
  echo "Template not found: $2"
  exit 1
fi

utils_folder=$(cd "$(dirname "$0")" && pwd)
definition_directory=$(cd "$1" && pwd)
template=$(cd "$(dirname "$2")" && pwd)/$(basename "$2")
payload=$(mktemp "${TMPDIR:-/tmp}/ccd-json-import.XXXXXX")
trap 'rm -f "$payload"' EXIT

et_cos_url=${ET_COS_URL:-http://localhost:8081}
ccd_def_base_url=${CCD_DEF_BASE_URL:-$et_cos_url}
ccd_def_url=${CCD_DEF_URL:-http://localhost:4452}
ccd_def_aac_url=${CCD_DEF_AAC_URL:-http://localhost:4454}

node - "$definition_directory" "$template" \
  "$et_cos_url" "$ccd_def_base_url" "$ccd_def_url" "$ccd_def_aac_url" > "$payload" <<'NODE'
const [directory, template, etCosUrl, ccdDefBaseUrl, ccdDefUrl, ccdDefAacUrl] = process.argv.slice(2);
const request = {
  directory,
  template,
  substitutions: {
    ET_COS_URL: etCosUrl,
    CCD_DEF_BASE_URL: ccdDefBaseUrl,
    CCD_DEF_URL: ccdDefUrl,
    CCD_DEF_AAC_URL: ccdDefAacUrl
  },
  excludedFilenamePatterns: ['*-prod.json']
};

process.stdout.write(`cftlib-json:${JSON.stringify(request)}`);
NODE

echo "Using JSON CCD definition directory ${definition_directory}"
"${utils_folder}/ccd-import-definition.sh" "$payload"
