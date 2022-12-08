#!/bin/bash

set -eu

function usage() {
    echo "Usage: $(basename $0) <command> [options]"
    echo
    echo "Commands:"
    echo "  set <project> <branch> [file://local_repository_path] - override and build project"
    echo "  unset <project...> - remove project override(s)"
    echo "  branches <project...> - list available branches"
    echo "  status - list current overrides and their status against upstream"
    echo "  update <project...> - update project overide to match upstream branch; and build"
    echo "  enable <project>|defaults|show - enable a compose file"
    echo "  disable <project> - disable a compose file"
    echo "  compose [<docker-compose command> [options]] - wrap docker compose for your configuration"
    echo
    exit 1
}

function ccd_login() {
    #docker logout hmctspublic.azurecr.io &>/dev/null
    az acr login --name hmctsprivate --subscription 8999dec3-0104-4a27-94ee-6588559729d1
    az acr login --name hmctspublic --subscription 8999dec3-0104-4a27-94ee-6588559729d1
}

function pullIDAM() {
    docker pull hmctspublic.azurecr.io/hmcts/rse/rse-idam-simulator:latest
}

function cleanCFTLIBDocker() {
  docker stop $(docker ps -a -q)
  docker rm $(docker ps -a -q)
  yes | docker volume prune
  docker network rm cftlib_default
}

function cleanAllDocker() {
docker container stop $(docker container ls -a -q)
docker system prune -a -f --volumes
}

function initDB() {
  if [ -z "$DB_URL" ] || [ -z "$ET_COS_DB_PASSWORD" ]; then
    echo "ERROR: Missing environment variables. Set value for 'DB_URL' and 'ET_COS_DB_PASSWORD'."
    exit 1
  fi

psql ${DB_URL} -v ON_ERROR_STOP=1 --username postgres --set USERNAME=et_cos --set PASSWORD=${ET_COS_DB_PASSWORD} <<-EOSQL
  CREATE USER :USERNAME WITH PASSWORD ':PASSWORD';
  CREATE DATABASE et_cos
    WITH OWNER = :USERNAME
    ENCODING = 'UTF-8'
    CONNECTION LIMIT = -1;
EOSQL
}

function importAllDefinitions() {
  ./import-ccd-definition-zsh a
  ./import-ccd-definition-zsh e
  ./import-ccd-definition-zsh s
}

function bootWithCCD() {
  ./gradlew bootWithCCD
}

if [ $# -lt 1 ]; then
    usage
fi

command=$1
shift
case $command in
    login)
        ccd_login
        ;;
    pullIDAM)
        pullIDAM
        ;;
    cleanCFTLIBDocker)
      cleanCFTLIBDocker
      ;;
    cleanAllDocker)
      cleanAllDocker
      ;;
    initDB)
      initDB
      ;;
    boot)
      bootWithCCD
      ;;
    importAllDefinitions)
      importAllDefinitions
      ;;
    *)
        usage
        ;;
esac
