#!/usr/bin/env bash

set -eu

if [ ! -z "$(docker container ls -a -q)" ]
then
docker container stop $(docker container ls -a -q)
fi
docker system prune -a -f --volumes
docker system prune -af
docker volume prune -af
docker builder prune -af
docker volume prune -a -f
