#!/usr/bin/env bash

set -eu

# Login to azure
az acr login --name hmctsprod

# Download Idam
docker pull hmctsprod.azurecr.io/hmcts/rse/rse-idam-simulator:latest
# Download Postgres
docker pull postgres:16
# Download elasticsearch
docker pull docker.elastic.co/elasticsearch/elasticsearch:9.2.4
# Download XUI Manage Cases
docker pull hmctsprod.azurecr.io/xui/webapp:latest
# Download XUI Manage Organisations
docker pull hmctsprod.azurecr.io/xui/mo-webapp:latest

# COMPOSE IMAGES
# Download AZURITE
docker pull mcr.microsoft.com/azure-storage/azurite
# Download wiremock
docker pull wiremock/wiremock:latest
# Download DM Store
docker pull hmctsprod.azurecr.io/dm/store:latest


#*************************WORK ALLOCATION*****************************
# Download camunda
docker pull hmctsprod.azurecr.io/camunda/bpm:latest
# Download WA Workflow API
docker pull hmctsprod.azurecr.io/wa/workflow-api:latest
# Download Case Event Handler
docker pull hmctsprod.azurecr.io/wa/case-event-handler:latest
# Download Task Monitor
docker pull hmctsprod.azurecr.io/wa/task-monitor:latest