#!/usr/bin/env bash

set -eu

# Login to azure
echo "********************************************"
echo "*********LOGGING INTO AZURE*********"
echo "********************************************"
az acr login --name hmctsprivate --subscription 8999dec3-0104-4a27-94ee-6588559729d1
az acr login --name hmctspublic --subscription 8999dec3-0104-4a27-94ee-6588559729d1
echo "********************************************"
echo "********KILLING RESIDUAL PROCESSES********"
echo "********************************************"
./bin/kill-residual-processes.sh
echo "********************************************"
echo "*******STOPPING ALL DOCKER CONTAINERS*******"
echo "********************************************"
docker stop $(docker ps -a -q)
echo "********************************************"
echo "****RUNNING CCD CALLBACKS CFTLIB PROFILE****"
echo "********************************************"
./gradlew bootWithCCD