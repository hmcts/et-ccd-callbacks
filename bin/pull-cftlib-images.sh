#!/usr/bin/env bash

set -eu

# Login to azure
az acr login --name hmctsprivate --subscription 8999dec3-0104-4a27-94ee-6588559729d1
az acr login --name hmctspublic --subscription 8999dec3-0104-4a27-94ee-6588559729d1

#CFTLIB IMAGES
# Download Idam
docker pull hmctspublic.azurecr.io/hmcts/rse/rse-idam-simulator:latest
# Download Postgres
docker pull postgres:12.4
# Download elasticsearch
docker pull docker.elastic.co/elasticsearch/elasticsearch:7.11.1
# Download logstash
docker pull docker.elastic.co/logstash/logstash:7.16.1
# Download XUI Manage Cases
docker pull hmctspublic.azurecr.io/xui/webapp:latest
# Download XUI Manage Organisations
docker pull hmctspublic.azurecr.io/xui/mo-webapp:latest
# COMPOSE IMAGES
# Download azurite latest version
docker pull mcr.microsoft.com/azure-storage/azurite:latest
# Download wiremock latest version
docker pull wiremock/wiremock:latest
