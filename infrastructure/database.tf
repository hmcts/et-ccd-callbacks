module "db" {
  source             = "git@github.com:hmcts/cnp-module-postgres?ref=master"
  product            = var.product
  component          = var.component
  location           = var.location_db
  env                = var.env
  database_name      = "et_ccd_callbacks"
  postgresql_user    = "et_ccd_callbacks"
  postgresql_version = "11"
  common_tags        = var.common_tags
  subscription       = var.subscription
}