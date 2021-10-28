#!/usr/bin/env bash
# Creates et_ccd_callbacks db, and its tables and functions that are required by et-ccd-callbacks

echo "Creating et_ccd_callbacks database"
psql postgresql://localhost:5050 -v ON_ERROR_STOP=1 -U postgres <<-EOSQL
  CREATE USER et_ccd_callbacks WITH PASSWORD 'et_ccd_callbacks';

  CREATE DATABASE et_ccd_callbacks
    WITH OWNER = et_ccd_callbacks
    ENCODING = 'UTF-8'
    CONNECTION LIMIT = -1;
EOSQL

set -e

echo "Running tbls_ethosCaseRefGen.sql"
psql postgresql://localhost:5050/et_ccd_callbacks -U et_ccd_callbacks -f ./tbls_ethosCaseRefGen.sql

echo "Running tbls_ethosMultipleCaseRefGen.sql"
psql postgresql://localhost:5050/et_ccd_callbacks -U et_ccd_callbacks -f ./tbls_ethosMultipleCaseRefGen.sql

echo "Running tbls_ethosSubMultipleCaseRefGen.sql"
psql postgresql://localhost:5050/et_ccd_callbacks -U et_ccd_callbacks -f ./tbls_ethosSubMultipleCaseRefGen.sql

echo "Running fn_ethosCaseRefGen.sql"
psql postgresql://localhost:5050/et_ccd_callbacks -U et_ccd_callbacks -f ./fn_ethosCaseRefGen.sql

echo "Running fn_ethosMultipleCaseRefGen.sql"
psql postgresql://localhost:5050/et_ccd_callbacks -U et_ccd_callbacks -f ./fn_ethosMultipleCaseRefGen.sql

echo "Running fn_ethosSubMultipleCaseRefGen.sql"
psql postgresql://localhost:5050/et_ccd_callbacks -U et_ccd_callbacks -f ./fn_ethosSubMultipleCaseRefGen.sql

echo "Running et_scripts/judge.sql"
psql postgresql://localhost:5050/et_ccd_callbacks -U et_ccd_callbacks -f ./et_scripts/judge.sql

echo "Running et_scripts/create-tables.sql"
psql postgresql://localhost:5050/et_ccd_callbacks -U et_ccd_callbacks -f ./et_scripts/create-tables.sql

echo "Running ET dataload"
psql postgresql://localhost:5050/et_ccd_callbacks -U et_ccd_callbacks -f ./et_scripts/data-load.sql