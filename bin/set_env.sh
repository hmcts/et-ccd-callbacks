#!/usr/bin/env bash

# Environment Variables Setup Script
echo "Setting environment variables..."

# Check if Azure CLI is available
if ! command -v az &> /dev/null; then
    echo "Error: Azure CLI not found. Please install it to retrieve Key Vault secrets."
    echo "Install with: brew install azure-cli"
    exit 1
fi

KEY_VAULT_NAME="et-aat"

# Pull Secrets from Azure Key Vault
export ET_LAUNCH_DARKLY_SDK_KEY=$(az keyvault secret show --name "launch-darkly-sdk-key" --vault-name "$KEY_VAULT_NAME" --query value -o tsv 2>/dev/null)
export XUI_LD_ID=$(az keyvault secret show --name "XUI-LD-ID" --vault-name "$KEY_VAULT_NAME" --query value -o tsv 2>/dev/null)
export GOV_NOTIFY_API_KEY=$(az keyvault secret show --name "gov-notify-api-key" --vault-name "$KEY_VAULT_NAME" --query value -o tsv 2>/dev/null)

# Static environment variables
export ET_COS_CFTLIB_DB_PASSWORD="postgres"
export SPRING_PROFILES_ACTIVE="cftlib"

###! optional - CCD Config paths - update these with your actual local repository paths before use!###
export ENGLANDWALES_CCD_CONFIG_PATH="$HOME/Source/et-ccd-definitions-englandwales"
export SCOTLAND_CCD_CONFIG_PATH="$HOME/Source/et-ccd-definitions-scotland"
export ADMIN_CCD_CONFIG_PATH="$HOME/Source/et-ccd-definitions-admin"

export ET_COS_SYSTEM_USER="admin@hmcts.net"
export ET_COS_SYSTEM_USER_PASSWORD="Password"

# Optional - set to false to prevent CCD definitions from being imported at startup
#export CFTLIB_IMPORT_CCD_DEFS_ON_BOOT="true"

printf '%s\n' \
  "XUI_LD_ID=${XUI_LD_ID}" \
  "ET_LAUNCH_DARKLY_SDK_KEY=${ET_LAUNCH_DARKLY_SDK_KEY}" \
  "GOV_NOTIFY_API_KEY=${GOV_NOTIFY_API_KEY}" \
  "ET_COS_CFTLIB_DB_PASSWORD=${ET_COS_CFTLIB_DB_PASSWORD}" \
  "SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE}" \
  "CFTLIB_IMPORT_CCD_DEFS_ON_BOOT=${CFTLIB_IMPORT_CCD_DEFS_ON_BOOT}" \
  "ET_COS_SYSTEM_USER=${ET_COS_SYSTEM_USER}" \
  "ET_COS_SYSTEM_USER_PASSWORD=${ET_COS_SYSTEM_USER_PASSWORD}" \
  "ENGLANDWALES_CCD_CONFIG_PATH=${ENGLANDWALES_CCD_CONFIG_PATH}" \
  "SCOTLAND_CCD_CONFIG_PATH=${SCOTLAND_CCD_CONFIG_PATH}" \
  "ADMIN_CCD_CONFIG_PATH=${ADMIN_CCD_CONFIG_PATH}" \
