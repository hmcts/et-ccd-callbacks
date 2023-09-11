#!/usr/bin/env bash

set -eu

echo "Checking port 4453"
processId=`lsof -i -P | grep LISTEN | grep :4453 | awk '{print $2}'`

if [ ! -z "$processId" ]
then
  echo "killing process with Id $processId"
  kill -9 "$processId"
else
  echo "there is no process using port 4453"
fi
echo "running ccd callbacks with cftlib"
./gradlew bootWithCCD
