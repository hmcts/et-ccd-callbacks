#!/bin/bash
# Create et_cos database

set -e

if [ -z "$ET_COS_DB_PASSWORD" ]; then
  echo "ERROR: Missing environment variable 'ET_COS_DB_PASSWORD'."
  exit 1
fi

psql postgresql://localhost:5050 -v ON_ERROR_STOP=1 --username postgres --set USERNAME=et_cos --set PASSWORD=${ET_COS_DB_PASSWORD} <<-EOSQL
  CREATE USER :USERNAME WITH PASSWORD ':PASSWORD';
  CREATE DATABASE et_cos
    WITH OWNER = :USERNAME
    ENCODING = 'UTF-8'
    CONNECTION LIMIT = -1;
EOSQL
