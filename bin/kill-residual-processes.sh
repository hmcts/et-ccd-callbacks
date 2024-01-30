#!/usr/bin/env bash

set -eu

echo "Checking port 4453"
processId_4453=`lsof -i -n -P | grep LISTEN | grep :4453 | awk '{print $2}'`

if [ ! -z "$processId_4453" ]
then
  echo "killing process with Id $processId_4453"
  kill -9 "$processId_4453"
else
  echo "There is no process running on port 4453"
fi

echo "Checking port 4455"
processId_4455=`lsof -i -n -P | grep LISTEN | grep :4455 | awk '{print $2}'`

if [ ! -z "$processId_4455" ]
then
  echo "killing process with Id $processId_4455"
  kill -9 "$processId_4455"
else
  echo "There is no process running on port 4455"
fi

echo "Checking port 8489"
processId_8489=`lsof -i -n -P | grep LISTEN | grep :8489 | awk '{print $2}'`

if [ ! -z "$processId_8489" ]
then
  echo "killing process with Id $processId_8489"
  kill -9 "$processId_8489"
else
  echo "There is no process running on port 8489"
fi
