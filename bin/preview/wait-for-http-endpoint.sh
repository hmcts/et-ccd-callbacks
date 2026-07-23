#!/usr/bin/env bash

set -euo pipefail

endpoint_url=${1:?"Usage: wait-for-http-endpoint.sh <URL> [endpoint name]"}
endpoint_name=${2:-HTTP endpoint}
retry_count=${ENDPOINT_RETRY_COUNT:-60}
retry_delay_seconds=${ENDPOINT_RETRY_DELAY_SECONDS:-5}
retry_max_time_seconds=${ENDPOINT_RETRY_MAX_TIME_SECONDS:-300}
connect_timeout_seconds=${ENDPOINT_CONNECT_TIMEOUT_SECONDS:-5}
request_timeout_seconds=${ENDPOINT_REQUEST_TIMEOUT_SECONDS:-15}

echo "Waiting for ${endpoint_name} to become available: ${endpoint_url}"

if ! curl \
  --fail \
  --silent \
  --show-error \
  --retry "${retry_count}" \
  --retry-all-errors \
  --retry-delay "${retry_delay_seconds}" \
  --retry-max-time "${retry_max_time_seconds}" \
  --connect-timeout "${connect_timeout_seconds}" \
  --max-time "${request_timeout_seconds}" \
  --output /dev/null \
  "${endpoint_url}"
then
  echo "${endpoint_name} did not become available within ${retry_max_time_seconds} seconds: ${endpoint_url}"
  exit 1
fi

echo "${endpoint_name} is available"
