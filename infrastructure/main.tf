provider "azurerm" {
  features {}
}

locals {
  tags = merge(var.common_tags,
    map(
      "environment", var.env,
      "managedBy", var.team_name,
      "Team Contact", var.team_contact
    )
  )

  api_mgmt_suffix = var.apim_suffix == "" ? var.env : var.apim_suffix
  api_mgmt_name   = "cft-api-mgmt-${local.api_mgmt_suffix}"
  api_mgmt_rg     = join("-", ["cft", var.env, "network-rg"])

  et_cos_url = join("", ["http://et-cos-", var.env, ".service.core-compute-", var.env, ".internal"])
  s2sUrl     = join("", ["http://rpe-service-auth-provider-", var.env, ".service.core-compute-", var.env, ".internal"])
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

resource "azurerm_key_vault_secret" "POSTGRES_HOST" {
  name         = "${var.component}-POSTGRES-HOST"
  value        = module.db.host_name
  key_vault_id = module.key-vault.key_vault_id
}

resource "azurerm_key_vault_secret" "POSTGRES_PORT" {
  name         = "${var.component}-POSTGRES-PORT"
  value        = "5432"
  key_vault_id = module.key-vault.key_vault_id
}

resource "azurerm_key_vault_secret" "POSTGRES_DATABASE" {
  name         = "${var.component}-POSTGRES-DATABASE"
  value        = module.db.postgresql_database
  key_vault_id = module.key-vault.key_vault_id
}

resource "azurerm_key_vault_secret" "POSTGRES-USER" {
  name         = "${var.component}-POSTGRES-USER"
  value        = module.db.user_name
  key_vault_id = module.key-vault.key_vault_id
}

resource "azurerm_key_vault_secret" "POSTGRES-PASS" {
  name         = "${var.component}-POSTGRES-PASS"
  value        = module.db.postgresql_password
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

data "azurerm_key_vault_secret" "s2s_client_id" {
  key_vault_id = module.key-vault.key_vault_id
  name         = "gateway-s2s-client-id"
}

provider "azurerm" {
  alias           = "aks-cftapps"
  subscription_id = var.aks_subscription_id
  features {}
}

resource "azurerm_api_management_user" "et_api_management_user" {
  api_management_name = local.api_mgmt_name
  resource_group_name = local.api_mgmt_rg
  first_name          = "Harpreet"
  last_name           = "Jhita"
  email               = "harpreet.jhita@justice.gov.uk"
  user_id             = ""
  provider            = azurerm.aks-cftapps
}
