#!/usr/bin/env bash

set -euo pipefail

STATIC_TABLES=(
  "judge"
  "venue"
  "room"
  "court_worker"
  "file_location"
)

PR_ID="${1:-${CHANGE_ID:-}}"
if [[ -z "${PR_ID}" ]]; then
  echo "Usage: $0 <pr-id>"
  echo "Missing PR id. Pass it as the first argument or set CHANGE_ID."
  exit 1
fi

SOURCE_HOST="${ET_COS_AAT_DB_HOST:-${SOURCE_ET_COS_DB_HOST:-et-cos-postgres-v15-aat.postgres.database.azure.com}}"
SOURCE_PORT="${ET_COS_AAT_DB_PORT:-${SOURCE_ET_COS_DB_PORT:-5432}}"
SOURCE_DB="${ET_COS_AAT_DB_NAME:-${SOURCE_ET_COS_DB_NAME:-et_cos}}"
SOURCE_USER="${ET_COS_AAT_DB_USER_NAME:-${SOURCE_ET_COS_DB_USER_NAME:-pgadmin}}"
SOURCE_PASSWORD="${ET_COS_AAT_DB_PASSWORD:-${SOURCE_ET_COS_DB_PASSWORD:-}}"
SOURCE_CONN_OPTIONS="${ET_COS_AAT_DB_CONN_OPTIONS:-${SOURCE_ET_COS_DB_CONN_OPTIONS:-sslmode=require}}"

TARGET_HOST="${ET_COS_PREVIEW_DB_HOST:-et-preview.postgres.database.azure.com}"
TARGET_PORT="${ET_COS_PREVIEW_DB_PORT:-5432}"
TARGET_DB="${ET_COS_PREVIEW_DB_NAME:-pr-${PR_ID}-et_cos}"
TARGET_USER="${ET_COS_PREVIEW_DB_USER_NAME:-hmcts}"
TARGET_PASSWORD="${ET_COS_PREVIEW_DB_PASSWORD:-${ET_PREVIEW_FLEXI_DB_PASSWORD:-${2:-}}}"
TARGET_CONN_OPTIONS="${ET_COS_PREVIEW_DB_CONN_OPTIONS:-sslmode=require}"

if [[ -z "${SOURCE_PASSWORD}" ]]; then
  echo "Missing source DB password. Set ET_COS_AAT_DB_PASSWORD or SOURCE_ET_COS_DB_PASSWORD."
  exit 1
fi

if [[ -z "${TARGET_PASSWORD}" ]]; then
  echo "Missing target DB password. Set ET_COS_PREVIEW_DB_PASSWORD or ET_PREVIEW_FLEXI_DB_PASSWORD."
  exit 1
fi

normalise_conn_options() {
  local conn_options="$1"
  echo "${conn_options#\?}"
}

connection_uri() {
  local user="$1"
  local host="$2"
  local port="$3"
  local database="$4"
  local conn_options
  conn_options="$(normalise_conn_options "$5")"

  if [[ -n "${conn_options}" ]]; then
    echo "postgresql://${user}@${host}:${port}/${database}?${conn_options}"
  else
    echo "postgresql://${user}@${host}:${port}/${database}"
  fi
}

SOURCE_URI="$(connection_uri "${SOURCE_USER}" "${SOURCE_HOST}" "${SOURCE_PORT}" "${SOURCE_DB}" "${SOURCE_CONN_OPTIONS}")"
TARGET_URI="$(connection_uri "${TARGET_USER}" "${TARGET_HOST}" "${TARGET_PORT}" "${TARGET_DB}" "${TARGET_CONN_OPTIONS}")"

DUMP_FILE="$(mktemp)"
trap 'rm -f "${DUMP_FILE}"' EXIT

PG_DUMP_TABLE_ARGS=()
TRUNCATE_TABLES=()
for table in "${STATIC_TABLES[@]}"; do
  PG_DUMP_TABLE_ARGS+=(--table="public.${table}")
  TRUNCATE_TABLES+=("public.${table}")
done

echo "Copying ET COS reference data from ${SOURCE_HOST}/${SOURCE_DB} to ${TARGET_HOST}/${TARGET_DB}"

PGPASSWORD="${SOURCE_PASSWORD}" pg_dump \
  --data-only \
  --no-owner \
  --no-privileges \
  "${PG_DUMP_TABLE_ARGS[@]}" \
  --dbname="${SOURCE_URI}" \
  --file="${DUMP_FILE}"

# Newer pg_dump clients emit settings that older preview Postgres servers do not support.
sed -i.bak '/^SET transaction_timeout = /d' "${DUMP_FILE}"
rm -f "${DUMP_FILE}.bak"

PGPASSWORD="${TARGET_PASSWORD}" psql \
  --set=ON_ERROR_STOP=1 \
  --dbname="${TARGET_URI}" \
  --command="TRUNCATE TABLE $(IFS=, ; echo "${TRUNCATE_TABLES[*]}") RESTART IDENTITY"

PGPASSWORD="${TARGET_PASSWORD}" psql \
  --set=ON_ERROR_STOP=1 \
  --dbname="${TARGET_URI}" \
  --file="${DUMP_FILE}"

echo "ET COS reference data import completed."
