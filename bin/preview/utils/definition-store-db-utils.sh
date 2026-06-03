#!/usr/bin/env bash

set -eu

CCD_DEFINITION_STORE_SCHEMA_TIMEOUT_SECONDS="${CCD_DEFINITION_STORE_SCHEMA_TIMEOUT_SECONDS:-300}"
CCD_DEFINITION_STORE_SCHEMA_POLL_SECONDS="${CCD_DEFINITION_STORE_SCHEMA_POLL_SECONDS:-10}"

normalise_conn_options() {
  local conn_options="$1"
  echo "${conn_options#\?}"
}

definition_store_connection_uri() {
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

definition_store_role_table_ready() {
  local result
  result="$(PGPASSWORD="${CCD_DEFINITION_STORE_PREVIEW_DB_PASSWORD}" psql \
    --quiet \
    --tuples-only \
    --no-align \
    --set=ON_ERROR_STOP=1 \
    --dbname="${CCD_DEFINITION_STORE_PREVIEW_DB_URI}" \
    --command="select to_regclass('public.role') is not null" 2>/dev/null)" || return 1

  [[ "${result}" == "t" ]]
}

wait_for_definition_store_schema() {
  local pr_id="${1:-${CHANGE_ID:-}}"

  CCD_DEFINITION_STORE_PREVIEW_DB_HOST="${CCD_DEFINITION_STORE_PREVIEW_DB_HOST:-et-preview.postgres.database.azure.com}"
  CCD_DEFINITION_STORE_PREVIEW_DB_PORT="${CCD_DEFINITION_STORE_PREVIEW_DB_PORT:-5432}"
  CCD_DEFINITION_STORE_PREVIEW_DB_USER_NAME="${CCD_DEFINITION_STORE_PREVIEW_DB_USER_NAME:-hmcts}"
  CCD_DEFINITION_STORE_PREVIEW_DB_NAME="${CCD_DEFINITION_STORE_PREVIEW_DB_NAME:-}"
  CCD_DEFINITION_STORE_PREVIEW_DB_PASSWORD="${CCD_DEFINITION_STORE_PREVIEW_DB_PASSWORD:-${ET_PREVIEW_FLEXI_DB_PASSWORD:-${POSTGRES_PASSWORD:-}}}"
  CCD_DEFINITION_STORE_PREVIEW_DB_CONN_OPTIONS="${CCD_DEFINITION_STORE_PREVIEW_DB_CONN_OPTIONS:-sslmode=require}"

  if [[ -z "${CCD_DEFINITION_STORE_PREVIEW_DB_NAME}" ]]; then
    if [[ -z "${pr_id}" ]]; then
      echo "Missing PR id. Set CHANGE_ID or CCD_DEFINITION_STORE_PREVIEW_DB_NAME."
      return 1
    fi
    CCD_DEFINITION_STORE_PREVIEW_DB_NAME="pr-${pr_id}-definition-store"
  fi

  if [[ -z "${CCD_DEFINITION_STORE_PREVIEW_DB_PASSWORD}" ]]; then
    echo "Missing definition-store DB password. Set CCD_DEFINITION_STORE_PREVIEW_DB_PASSWORD, ET_PREVIEW_FLEXI_DB_PASSWORD or POSTGRES_PASSWORD."
    return 1
  fi

  CCD_DEFINITION_STORE_PREVIEW_DB_URI="$(definition_store_connection_uri \
    "${CCD_DEFINITION_STORE_PREVIEW_DB_USER_NAME}" \
    "${CCD_DEFINITION_STORE_PREVIEW_DB_HOST}" \
    "${CCD_DEFINITION_STORE_PREVIEW_DB_PORT}" \
    "${CCD_DEFINITION_STORE_PREVIEW_DB_NAME}" \
    "${CCD_DEFINITION_STORE_PREVIEW_DB_CONN_OPTIONS}")"

  local deadline=$((SECONDS + CCD_DEFINITION_STORE_SCHEMA_TIMEOUT_SECONDS))
  while ! definition_store_role_table_ready; do
    if (( SECONDS >= deadline )); then
      echo "Timed out waiting for CCD definition-store schema in ${CCD_DEFINITION_STORE_PREVIEW_DB_HOST}/${CCD_DEFINITION_STORE_PREVIEW_DB_NAME}."
      echo "The CCD definition-store app must start and run migrations before roles or definitions can be imported."
      return 1
    fi

    echo "Waiting for CCD definition-store schema in ${CCD_DEFINITION_STORE_PREVIEW_DB_HOST}/${CCD_DEFINITION_STORE_PREVIEW_DB_NAME}..."
    sleep "${CCD_DEFINITION_STORE_SCHEMA_POLL_SECONDS}"
  done
}
