#!/usr/bin/env bash
# This script is used to run the WA reconfiguration task.
# It should be run from the root of the project and assumes the user is running this locally using CFTLib
# Usage: ./bin/tasks/BatchReconfigurationTask.sh <CRON_RECONFIGURATION_CASE_IDS>
# Example: ./bin//tasks/BatchReconfigurationTask.sh 1756978619188973
set -eu

SERVER_PORT=4551 TASK_NAME=BatchReconfigurationTask SPRING_PROFILES_ACTIVE=cftlib CRON_RECONFIGURATION_CASE_IDS=$1 ./gradlew bootRun
