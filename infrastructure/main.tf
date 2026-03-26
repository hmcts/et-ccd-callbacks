provider "azurerm" {
  features {}
}

provider "azurerm" {
  features {}
  skip_provider_registration = true
  alias                      = "private_endpoint"
  subscription_id            = var.aks_subscription_id
}

provider "azurerm" {
  alias           = "aks-cftapps"
  subscription_id = var.aks_subscription_id
  features {}
}

locals {
  tagEnv = var.env == "aat" ? "staging" : var.env == "perftest" ? "testing" : var.env
  tags = merge(var.common_tags,
    tomap({
      "environment"  = local.tagEnv,
      "managedBy"    = var.team_name,
      "Team Contact" = var.team_contact,
      "application"  = "employment-tribunals",
      "businessArea" = var.businessArea
      "builtFrom"    = "et-ccd-callbacks"
    })
  )
  api_mgmt_suffix      = var.apim_suffix == "" ? var.env : var.apim_suffix
  api_mgmt_name        = "cft-api-mgmt-${local.api_mgmt_suffix}"
  api_mgmt_rg          = join("-", ["cft", var.env, "network-rg"])
  et_ccd_callbacks_url = join("", ["http://et-cos-", var.env, ".service.core-compute-", var.env, ".internal"])
  s2sUrl               = join("", ["http://rpe-service-auth-provider-", var.env, ".service.core-compute-", var.env, ".internal"])
}


resource "azurerm_resource_group" "rg" {
  name     = "${var.product}-${var.component}-${var.env}"
  location = var.location
  tags     = local.tags
}

data "azurerm_user_assigned_identity" "et-identity" {
  name                = "${var.product}-${var.env}-mi"
  resource_group_name = "managed-identities-${var.env}-rg"
}

resource "azurerm_key_vault_secret" "POSTGRES_PORT" {
  name         = "${var.component}-POSTGRES-PORT"
  value        = "5432"
  key_vault_id = module.key-vault.key_vault_id
}

data "azurerm_key_vault" "s2s_vault" {
  name                = "s2s-${var.env}"
  resource_group_name = "rpe-service-auth-provider-${var.env}"
}

data "azurerm_key_vault_secret" "et_cos_s2s_key" {
  name         = "microservicekey-et-cos"
  key_vault_id = data.azurerm_key_vault.s2s_vault.id
}

resource "azurerm_key_vault_secret" "et_cos_s2s_secret" {
  name         = "et-cos-s2s-secret"
  value        = data.azurerm_key_vault_secret.et_cos_s2s_key.value
  key_vault_id = module.key-vault.key_vault_id
}

data "azurerm_key_vault" "et_sya_api_vault" {
  name                = "et-sya-api-${var.env}"
  resource_group_name = "et-sya-api-${var.env}"
}

data "azurerm_key_vault_secret" "sya_api_idam_client_secret" {
  name         = "acas-idam-client-secret"
  key_vault_id = data.azurerm_key_vault.et_sya_api_vault.id
}

data "azurerm_key_vault_secret" "sya_et1_service_owner_email" {
  name         = "et1-service-owner-notification-email"
  key_vault_id = data.azurerm_key_vault.et_sya_api_vault.id
}

data "azurerm_key_vault_secret" "sya_et1_core_team_slack_email" {
  name         = "et1-ecm-dts-core-team-slack-notification-email"
  key_vault_id = data.azurerm_key_vault.et_sya_api_vault.id
}

data "azurerm_key_vault_secret" "et_api_caseworker_username" {
  name         = "cos-system-user"
  key_vault_id = module.key-vault.key_vault_id
}

data "azurerm_key_vault_secret" "et_api_caseworker_password" {
  name         = "cos-system-user-password"
  key_vault_id = module.key-vault.key_vault_id
}

resource "azurerm_key_vault_secret" "et_caseworker_user_name" {
  key_vault_id = module.key-vault.key_vault_id
  name         = "et-api-caseworker-user-name"
  value        = data.azurerm_key_vault_secret.et_api_caseworker_username.value
}

resource "azurerm_key_vault_secret" "et_caseworker_password" {
  key_vault_id = module.key-vault.key_vault_id
  name         = "et-api-caseworker-password"
  value        = data.azurerm_key_vault_secret.et_api_caseworker_password.value
}

resource "azurerm_key_vault_secret" "sya_idam_client_secret" {
  key_vault_id = module.key-vault.key_vault_id
  name         = "acas-idam-client-secret"
  value        = data.azurerm_key_vault_secret.sya_api_idam_client_secret.value
}

resource "azurerm_key_vault_secret" "sya_et1_service_owner_notification_email" {
  key_vault_id = module.key-vault.key_vault_id
  name         = "et1-service-owner-notification-email"
  value        = data.azurerm_key_vault_secret.sya_et1_service_owner_email.value
}

resource "azurerm_key_vault_secret" "sya_et1_core_team_slack_notification_email" {
  key_vault_id = module.key-vault.key_vault_id
  name         = "et1-ecm-dts-core-team-slack-notification-email"
  value        = data.azurerm_key_vault_secret.sya_et1_core_team_slack_email.value
}
