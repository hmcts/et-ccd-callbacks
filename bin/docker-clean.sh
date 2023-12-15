#!/usr/bin/env bash

set -eu

docker container stop $(docker container ls -a -q)

docker system prune -af
docker volume prune -af
docker builder prune -af