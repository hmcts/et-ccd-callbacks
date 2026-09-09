terraform {
  backend "azurerm" {}

  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "4.79.0"
    }
    postgresql = {
      source  = "cyrilgdn/postgresql"
      version = "~> 1.26"
    }
    random = {
      source  = "hashicorp/random"
      version = "~> 3.6"
    }
  }
}
