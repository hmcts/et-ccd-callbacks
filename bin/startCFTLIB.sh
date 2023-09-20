#!/usr/bin/env bash

set -eu

./bin/kill-residual-processes.sh

echo "running ccd callbacks with cftlib"

./gradlew bootWithCCD