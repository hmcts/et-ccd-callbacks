#!/usr/bin/env bash
# This script is used to run the notice of change fields update task.
# Fields to be updated are respondent's representative id, representative's id if not exists and representative's role
# It should be run from the root of the project and assumes the user is running this locally using CFTLib
# Usage: ./bin/tasks/NoticeOfChangeFieldsTask_Run.sh
set -eu

SERVER_PORT=4551 TASK_NAME=NoticeOfChangeFieldsTask SPRING_PROFILES_ACTIVE=cftlib ./gradlew bootRun
