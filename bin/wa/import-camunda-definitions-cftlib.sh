#!/usr/bin/env bash

set -eu

# Set deployment source and tenant id variables
DEPLOYMENT_SOURCE="et"
TENANT_ID="employment"

if [[ -z "${WA_STANDALONE_TASK_REPO_PATH:-}" ]]; then
  echo 1>&2 "Error: environment variable WA_STANDALONE_TASK_REPO_PATH not set"
  exit 1
fi

if [[ -z "${WA_TASK_CONFIGURATION_REPO_PATH:-}" ]]; then
  echo 1>&2 "Error: environment variable WA_TASK_CONFIGURATION_REPO_PATH not set"
  exit 1
fi

camundaDeployUrl="${CAMUNDA_URL:-http://localhost:9090}/engine-rest/deployment/create"

camundaFilepath="$WA_STANDALONE_TASK_REPO_PATH/src/main/resources"
if [ ! -d "${camundaFilepath}" ]; then
  echo "Directory with Camunda definition files is missing: ${camundaFilepath}";
  exit 1
fi

echo "Getting s2s token"
serviceToken="$("$(dirname "$0")/../utils/lease-service-token.sh" "et_cos")"

echo "Importing Camunda BPMN files to ${camundaDeployUrl}"
echo
# Load BPMN files
find "${camundaFilepath}" -name '*.bpmn' -print0 | while IFS= read -r -d '' file; do
  echo "Importing $(basename "${file}")";
  uploadResponse=$(curl -s -o - -w "\n%{http_code}" --show-error -X POST \
    "$camundaDeployUrl" \
    -H "Accept: application/json" \
    -H "ServiceAuthorization: Bearer ${serviceToken}" \
    -F "deployment-name=$(basename "${file}")" \
    -F "deploy-changed-only=true" \
    -F "file=@${camundaFilepath}/$(basename ${file})")

  upload_http_code=$(echo "$uploadResponse" | tail -n1)
  upload_response_content=$(echo "$uploadResponse" | sed '$d')

  if [[ "${upload_http_code}" == '200' ]]; then
    echo "Imported $(basename "${file}")";
  else
    echo "$(basename "${file}") upload failed with http code ${upload_http_code} and response (${upload_response_content})"
  fi
done

echo

# Load DMN files
camundaFilepath="$WA_TASK_CONFIGURATION_REPO_PATH/src/main/resources"
if [ ! -d "${camundaFilepath}" ]; then
  echo "Directory with Camunda definition files is missing: ${camundaFilepath}";
  exit 1
fi

echo "Importing Camunda DMN files to ${camundaDeployUrl}"
echo

find "${camundaFilepath}" -name '*.dmn' -print0 | while IFS= read -r -d '' file; do
  echo "Importing $(basename "${file}")";
  uploadResponse=$(curl -s -o - -w "\n%{http_code}" --show-error -X POST \
    "$camundaDeployUrl" \
    -H "Accept: application/json" \
    -H "ServiceAuthorization: Bearer ${serviceToken}" \
    -F "deployment-name=$(basename "${file}")" \
    -F "deploy-changed-only=true" \
    -F "deployment-source=${DEPLOYMENT_SOURCE}" \
    -F "tenant-id=${TENANT_ID}" \
    -F "file=@${camundaFilepath}/$(basename "${file}")")

  upload_http_code=$(echo "$uploadResponse" | tail -n1)
  upload_response_content=$(echo "$uploadResponse" | sed '$d')

  if [[ "${upload_http_code}" == '200' ]]; then
    continue;
  fi

  echo "$(basename "${file}") upload failed with http code ${upload_http_code} and response (${upload_response_content})";
  continue;
done
