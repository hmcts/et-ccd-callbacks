#!/usr/bin/env bash

set -eu

dir=$(dirname ${0})

cd ${dir}/../src/cftlib/compose
docker compose -f dm-store.yml stop
