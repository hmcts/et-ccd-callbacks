#!/usr/bin/env bash

set -euo pipefail

camunda_base_url=${CAMUNDA_BASE_URL:-http://localhost:9404}
readiness_url="${camunda_base_url}/engine-rest/version"

echo "Waiting for Camunda ingress to become available: ${readiness_url}"

if ! curl \
  --fail \
  --silent \
  --show-error \
  --retry 60 \
  --retry-all-errors \
  --retry-delay 5 \
  --retry-max-time 300 \
  --connect-timeout 5 \
  --max-time 15 \
  --output /dev/null \
  "${readiness_url}"
then
  echo "Camunda ingress did not become available within 300 seconds: ${readiness_url}"
  exit 1
fi

echo "Camunda ingress is available"
