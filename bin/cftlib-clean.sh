#!/usr/bin/env bash

set -eu

containers="$(docker ps -a -q)"
if [ -n "$containers" ]; then
  docker stop $containers
  docker rm $containers
fi

yes | docker volume prune

if docker network inspect cftlib_default >/dev/null 2>&1; then
  docker network rm cftlib_default
fi
