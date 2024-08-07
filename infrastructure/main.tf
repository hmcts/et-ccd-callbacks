provider "azurerm" {
  features {}
}

provider "azurerm" {
  features {}
  skip_provider_registration = true
  alias                      = "private_endpoint"
  subscription_id            = var.aks_subscription_id
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
