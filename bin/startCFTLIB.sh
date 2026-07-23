#!/usr/bin/env bash

set -eu

# Login to azure
echo "Logging into Azure"
az acr login --name hmctsprivate --subscription 8999dec3-0104-4a27-94ee-6588559729d1
az acr login --name hmctspublic --subscription 8999dec3-0104-4a27-94ee-6588559729d1
az acr login --name hmctsprod

echo "Killing any residual processes"
./bin/kill-residual-processes.sh

echo "running ccd callbacks with cftlib"
./gradlew bootWithCCD

