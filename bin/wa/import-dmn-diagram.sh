#!/usr/bin/env bash

set -euo pipefail
workspace=${1}
tenant_id=${2}
product=${3}

s2sSecret=${ET_COS_S2S_KEY:-AABBCCDDEEFFGGHH}

#if [[ "${env}" == 'prod' ]]; then
#  s2sSecret=${ET_COS_S2S_KEY}
#fi

serviceToken=$($(realpath $workspace)/bin/preview/idam-lease-service-token.sh et_cos \
  $(docker run --rm toolbelt/oathtool --totp -b ${s2sSecret}))

dmnFilepath="$(realpath $workspace)/resources"

echo "${CAMUNDA_BASE_URL} import-dmn-diagram.sh line 19"

for file in $(find ${dmnFilepath} -name '*.dmn')
do
  curl_exit_code=0
  if uploadResponse=$(curl --insecure -v --silent -w "\n%{http_code}" --show-error --fail-with-body \
    --retry 12 --retry-all-errors --retry-delay 5 --retry-max-time 120 -X POST \
    ${CAMUNDA_BASE_URL:-http://localhost:9404}/engine-rest/deployment/create \
    -H "Accept: application/json" \
    -H "ServiceAuthorization: Bearer ${serviceToken}" \
    -F "deployment-name=$(basename ${file})" \
    -F "deploy-changed-only=true" \
    -F "deployment-source=$product" \
    ${tenant_id:+'-F' "tenant-id=$tenant_id"} \
    -F "file=@${dmnFilepath}/$(basename ${file})"); then
    curl_exit_code=0
  else
    curl_exit_code=$?
  fi

upload_http_code=$(echo "$uploadResponse" | tail -n1)
upload_response_content=$(echo "$uploadResponse" | sed '$d')

if [[ "${curl_exit_code}" -eq 0 && "${upload_http_code}" == '200' ]]; then
  echo "$(basename ${file}) diagram uploaded successfully (${upload_response_content})"
  continue;
fi

echo "$(basename ${file}) upload failed with http code ${upload_http_code} and response (${upload_response_content})"
if [[ "${curl_exit_code}" -eq 0 ]]; then
  curl_exit_code=1
fi
exit "${curl_exit_code}"

done

bpmnFilepath="$(realpath $workspace)/camunda"
if [ -d ${bpmnFilepath} ]
then
  for file in $(find ${bpmnFilepath} -name '*.bpmn')
  do
    curl_exit_code=0
    if uploadResponse=$(curl --insecure -v --silent -w "\n%{http_code}" --show-error --fail-with-body \
      --retry 12 --retry-all-errors --retry-delay 5 --retry-max-time 120 -X POST \
      ${CAMUNDA_BASE_URL:-http://localhost:9404}/engine-rest/deployment/create \
      -H "Accept: application/json" \
      -H "ServiceAuthorization: Bearer ${serviceToken}" \
      -F "deployment-name=$(basename ${file})" \
      -F "deploy-changed-only=true" \
      -F "file=@${bpmnFilepath}/$(basename ${file})"); then
      curl_exit_code=0
    else
      curl_exit_code=$?
    fi

  upload_http_code=$(echo "$uploadResponse" | tail -n1)
  upload_response_content=$(echo "$uploadResponse" | sed '$d')

  if [[ "${curl_exit_code}" -eq 0 && "${upload_http_code}" == '200' ]]; then
    echo "$(basename ${file}) diagram uploaded successfully (${upload_response_content})"
    continue;
  fi

  echo "$(basename ${file}) upload failed with http code ${upload_http_code} and response (${upload_response_content})"
  if [[ "${curl_exit_code}" -eq 0 ]]; then
    curl_exit_code=1
  fi
  exit "${curl_exit_code}"

  done
  exit 0;
fi
