#!/usr/bin/env bash
# Creates et_cos db, and its tables and functions that are required by et-cos

echo "Creating et_cos database"
psql postgresql://localhost:5050 -v ON_ERROR_STOP=1 -U postgres <<-EOSQL
  CREATE USER et_cos WITH PASSWORD 'et_cos';

  CREATE DATABASE et_cos
    WITH OWNER = et_cos
    ENCODING = 'UTF-8'
    CONNECTION LIMIT = -1;

EOSQL

set -e

echo "Running tbls_ethosCaseRefGen.sql"
psql postgresql://localhost:5050/et_cos -U et_cos -f ./tbls_ethosCaseRefGen.sql

echo "Running tbls_ethosMultipleCaseRefGen.sql"
psql postgresql://localhost:5050/et_cos -U et_cos -f ./tbls_ethosMultipleCaseRefGen.sql

echo "Running tbls_ethosSubMultipleCaseRefGen.sql"
psql postgresql://localhost:5050/et_cos -U et_cos -f ./tbls_ethosSubMultipleCaseRefGen.sql

echo "Running fn_ethosCaseRefGen.sql"
psql postgresql://localhost:5050/et_cos -U et_cos -f ./fn_ethosCaseRefGen.sql

echo "Running fn_ethosMultipleCaseRefGen.sql"
psql postgresql://localhost:5050/et_cos -U et_cos -f ./fn_ethosMultipleCaseRefGen.sql

echo "Running fn_ethosSubMultipleCaseRefGen.sql"
psql postgresql://localhost:5050/et_cos -U et_cos -f ./fn_ethosSubMultipleCaseRefGen.sql