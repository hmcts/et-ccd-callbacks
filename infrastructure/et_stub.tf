module "api-etstub-mgmt-product" {
  source = "git@github.com:hmcts/cnp-module-api-mgmt-product?ref=master"

  api_mgnt_name                 = local.api_mgmt_name
  api_mgnt_rg                   = local.api_mgmt_rg
  name                          = var.etstub_product_name
  product_access_control_groups = ["developers"]
  approval_required             = "false"
  subscription_required         = "true"
  providers = {
    azurerm = azurerm.aks-cftapps
  }
}

module "etstub-mgmt-api" {
  source = "git@github.com:hmcts/cnp-module-api-mgmt-api?ref=master"

  api_mgmt_name = local.api_mgmt_name
  api_mgmt_rg   = local.api_mgmt_rg
  revision      = "1"
  service_url   = local.et_cos_url
  product_id    = module.api-casedata-mgmt-product.product_id
  display_name  = "Get ET Stub Api"
  path          = "et-cos"
  protocols     = ["http", "https"]

  providers = {
    azurerm = azurerm.aks-cftapps
  }
}

data "template_file" "etstub_policy_template" {
  template = file(join("", [path.module, "/template/api-policy.xml"]))

  vars = {
    s2s_client_id                   = data.azurerm_key_vault_secret.s2s_client_id.value
    s2s_client_secret               = data.azurerm_key_vault_secret.et_cos_s2s_key.value
    s2s_base_url                    = local.s2sUrl
  }
}

module "mdl-et-stub-policy" {
  source = "git@github.com:hmcts/cnp-module-api-mgmt-api-policy?ref=master"
  api_mgmt_name = local.api_mgmt_name
  api_mgmt_rg = local.api_mgmt_rg

  api_name = module.api-etstub-mgmt-product.name
  api_policy_xml_content = data.template_file.etstub_policy_template

  providers     = {
    azurerm = azurerm.aks-cftapps
  }
}

resource "azurerm_api_management_subscription" "etstub_subscription" {
  api_management_name = local.api_mgmt_name
  resource_group_name = local.api_mgmt_rg
  user_id = azurerm_api_management_user.et_api_management_user.id
  product_id = module.api-etstub-mgmt-product.id
  display_name        = "ET Stub Subscription"
  state = "active"
  provider = azurerm.aks-cftapps
}

resource "azurerm_key_vault_secret" "etstub_subscription_key" {
  key_vault_id = module.key-vault.key_vault_id
  name         = "etstub-subscription-key"
  value        = azurerm_api_management_subscription.etstub_subscription.primary_key
}