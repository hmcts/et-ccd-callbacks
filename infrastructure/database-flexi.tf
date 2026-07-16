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
      name : "et_cos",
      schemas_for_reader_access = ["public", "ccd"]
    }
  ]
  pgsql_version                  = "15"
  pgsql_storage_mb               = var.env == "prod" ? 262144 : 65536
  admin_user_object_id           = var.jenkins_AAD_objectId
  force_user_permissions_trigger = "2"
  enable_db_report_privileges    = true
  pgsql_server_configuration = [
    {
      name  = "azure.extensions"
      value = "postgres_fdw,pgcrypto"
    },
    {
      "name" : "backslash_quote",
      "value" : "on"
    },
    {
      name  = "pg_qs.query_capture_mode"
      value = "top"
    },
    {
      name  = "pg_qs.store_query_plans"
      value = "on"
    },
    {
      name  = "pgms_wait_sampling.query_capture_mode"
      value = "all"
    }
  ]
  auto_grow_enabled = true
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
