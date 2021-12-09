module "db" {
  source             = "git@github.com:hmcts/cnp-module-postgres?ref=master"
  product            = var.product
  component          = var.component
  location           = var.location_db
  env                = var.env
  database_name      = "et_cos"
  postgresql_user    = "et_cos"
  postgresql_version = "11"
  common_tags        = local.tags
  subscription       = var.subscription
}