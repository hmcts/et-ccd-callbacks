locals {
  sdp_reader_name = "sdp_reader"
  sdp_database    = "et_cos"
  sdp_schema      = "ccd"
}

resource "random_password" "sdp_reader" {
  length           = 32
  special          = true
  override_special = "!#$%&*+-=?@^_"
}

resource "postgresql_role" "sdp_reader" {
  name                = local.sdp_reader_name
  login               = true
  password_wo         = random_password.sdp_reader.result
  password_wo_version = random_password.sdp_reader.id

  superuser                 = false
  create_database           = false
  create_role               = false
  inherit                   = false
  replication               = false
  bypass_row_level_security = false
  connection_limit          = 10
}

resource "postgresql_grant" "sdp_reader_database_connect" {
  database    = local.sdp_database
  role        = postgresql_role.sdp_reader.name
  object_type = "database"
  privileges  = ["CONNECT"]
}

resource "postgresql_grant" "sdp_reader_ccd_schema" {
  database    = local.sdp_database
  role        = postgresql_role.sdp_reader.name
  schema      = local.sdp_schema
  object_type = "schema"
  privileges  = ["USAGE"]
}

resource "postgresql_grant" "sdp_reader_ccd_tables" {
  database    = local.sdp_database
  role        = postgresql_role.sdp_reader.name
  schema      = local.sdp_schema
  object_type = "table"
  privileges  = ["SELECT"]
}

resource "postgresql_default_privileges" "sdp_reader_ccd_tables" {
  database    = local.sdp_database
  owner       = module.postgres.username
  role        = postgresql_role.sdp_reader.name
  schema      = local.sdp_schema
  object_type = "table"
  privileges  = ["SELECT"]
}

resource "azurerm_key_vault_secret" "sdp_postgres_readonly_user" {
  name         = "sdp-postgres-readonly-user"
  value        = postgresql_role.sdp_reader.name
  key_vault_id = module.key-vault.key_vault_id
}

resource "azurerm_key_vault_secret" "sdp_postgres_readonly_password" {
  name         = "sdp-postgres-readonly-password"
  value        = random_password.sdp_reader.result
  key_vault_id = module.key-vault.key_vault_id
}
