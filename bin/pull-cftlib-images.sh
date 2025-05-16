#!/usr/bin/env bash

set -eu

# Login to azure
az acr login --name hmctsprivate --subscription 8999dec3-0104-4a27-94ee-6588559729d1
az acr login --name hmctspublic --subscription 8999dec3-0104-4a27-94ee-6588559729d1

# Download Idam
docker pull hmctspublic.azurecr.io/hmcts/rse/rse-idam-simulator:latest
# Download Postgres
docker pull postgres:12.4
# Download elasticsearch
docker pull docker.elastic.co/elasticsearch/elasticsearch:7.11.1
# Download logstash
docker pull docker.elastic.co/logstash/logstash:7.16.1
# Download XUI Manage Cases
# docker pull hmctspublic.azurecr.io/xui/webapp:latest
# Download XUI Manage Cases - Without Work Allocation
docker pull tdmehmet/xui-webapp:withoutWA
docker tag tdmehmet/xui-webapp:withoutWA hmctspublic.azurecr.io/xui/webapp:latest
# Download XUI Manage Organisations
docker pull hmctspublic.azurecr.io/xui/mo-webapp:latest

# COMPOSE IMAGES
# Download AZURITE
docker pull mcr.microsoft.com/azure-storage/azurite
# Download wiremock
docker pull wiremock/wiremock:latest


#*************************WORK ALLOCATION*****************************
# Download camunda
# docker pull hmctsprivate.azurecr.io/camunda/bpm:latest
# Download CCD Message Publisher
# docker pull hmctspublic.azurecr.io/ccd/message-publisher:latest
# Download WA Workflow API
# docker pull hmctspublic.azurecr.io/wa/workflow-api:latest
# Download WA Task Management API
# docker pull hmctspublic.azurecr.io/wa/task-management-api:latest
# Download Case Event Handler
# docker pull hmctspublic.azurecr.io/wa/case-event-handler:latest
# Download Task Monitor
# docker pull hmctspublic.azurecr.io/wa/task-monitor:latest
# Download