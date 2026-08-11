##
## Additional providers required for the SDP read-only PostgreSQL login.
## The postgresql provider connects to the Flexible Server through the module's
## administrator credentials so that the LOGIN role can be created and granted
## privileges at apply time.
##


provider "postgresql" {
  host     = module.postgres.fqdn
  port     = 5432
  database = "et_cos"
  username = module.postgres.username
  password = module.postgres.password
  sslmode  = "require"

  # The Flexible Server is private; the runner executing terraform apply must
  # have network access via VNet/bastion or private DNS resolution.
  connect_timeout = 15
}
