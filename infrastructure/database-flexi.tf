# ET COS FlexiDB
module "postgres" {
  source = "git@github.com:hmcts/terraform-module-postgresql-flexible?ref=master"
  env    = var.env
  providers = {
    azurerm.postgres_network = azurerm.private_endpoint
  }
  name          = "et-cos-postgres-v15"
  product       = var.product
  component     = var.component
  business_area = var.businessArea
  common_tags   = local.tags
  pgsql_databases = [
    {
      name : "et_cos"
    }
  ]
  pgsql_version                  = "15"
  admin_user_object_id           = var.jenkins_AAD_objectId
  force_user_permissions_trigger = "2"
}

resource "azurerm_key_vault_secret" "et_cos_postgres_user_v15" {
  name         = "et-cos-postgres-user-v15"
  value        = module.postgres.username
  key_vault_id = module.key-vault.key_vault_id
}

resource "azurerm_key_vault_secret" "et_cos_postgres_password_v15" {
  name         = "et-cos-postgres-password-v15"
  value        = module.postgres.password
  key_vault_id = module.key-vault.key_vault_id
}

resource "azurerm_key_vault_secret" "et_cos_postgres_host_v15" {
  name         = "et-cos-postgres-host-v15"
  value        = module.postgres.fqdn
  key_vault_id = module.key-vault.key_vault_id
}

resource "azurerm_key_vault_secret" "et_cos_postgres_port_v15" {
  name         = "et-cos-postgres-port-v15"
  value        = "5432"
  key_vault_id = module.key-vault.key_vault_id
}
