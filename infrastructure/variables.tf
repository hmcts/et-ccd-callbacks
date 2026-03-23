variable "product" {
  default = "et"
}

variable "component" {
  default = "cos"
}

variable "location" {
  default = "UK South"
}

variable "location_db" {
  default = "UK South"
}

variable "env" {
}

variable "subscription" {
}

variable "tenant_id" {}

variable "jenkins_AAD_objectId" {
  description = "(Required) The Azure AD object ID of a user, service principal or security group in the Azure Active Directory tenant for the vault. The object ID must be unique for the list of access policies."
}

variable "deployment_namespace" {
  default = ""
}

variable "common_tags" {
  type = map(string)
}

variable "team_name" {
  description = "Team name"
  default     = "Employment Tribunals"
}

variable "team_contact" {
  description = "Team contact"
  default     = "#et-tech"
}

variable "aks_subscription_id" {}

variable "businessArea" {
  default = "CFT"
}

variable "apim_suffix" {
  default = ""
}

variable "et_ccd_callbacks_product_name" {
  type    = string
  default = "et-ccd-callbacks"
}

variable "et_ccd_callbacks_s2s_client_id" {
  type    = string
  default = "et_cos"
}

variable "acas_swagger_url" {
  default = "https://raw.githubusercontent.com/hmcts/reform-api-docs/master/docs/specs/et-acas-api-nonprod.json"
}
