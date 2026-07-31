#!/usr/bin/env bash
# This script is used to run the notice of change fields update task.
# Fields to be updated are respondent's representative id, representative's id if not exists and representative's role
# It should be run from the root of the project and assumes the user is running this locally using CFTLib
# Usage: ./bin/tasks/NoticeOfChangeFieldsTask_Run.sh
set -eu

echo "Checking port 4551"
processId_4551=`lsof -i -n -P | grep LISTEN | grep :4551 | awk '{print $2}'`

if [ ! -z "$processId_4551" ]
then
  echo "killing process with Id $processId_4551"
  kill -9 "$processId_4551"
else
  echo "There is no process running on port 4551"
fi

SERVER_PORT=4551 TASK_NAME=NoticeOfChangeFieldsTask SPRING_PROFILES_ACTIVE=cftlib ./gradlew bootRun
