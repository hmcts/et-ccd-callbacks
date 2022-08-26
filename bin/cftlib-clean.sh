#!/usr/bin/env bash

set -eu

docker stop $(docker ps -a -q)
docker rm $(docker ps -a -q)
docker volume prune
docker network rm cftlib_default
