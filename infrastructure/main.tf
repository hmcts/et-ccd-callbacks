provider "azurerm" {
  features {}
}

locals {
  resource_group_name = "${var.product}-${var.env}"

  common_tags = {
    "environment"  = var.env
    "managedBy"    = var.team_name
    "Team Contact" = var.team_contact
  }
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

resource "azurerm_key_vault_secret" "et_ccd_callbacks_s2s_secret" {
  name         = "et-ccd-callbacks-s2s-secret"
  value        = "microservicekey_et_ccd_callbacks"
  key_vault_id = module.key-vault.key_vault_id
}

resource "azurerm_key_vault_secret" "tornado-access-key" {
  name         = "tornado-access-key"
  value        = "change-me"
  key_vault_id = module.key-vault.key_vault_id

  lifecycle {
    ignore_changes = [ value ]
  }
}