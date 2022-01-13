# provider
terraform {
  required_providers {
    azurerm = {
      source = "hashicorp/azurerm"
      version = "2.91.0"
    }
  }
}

provider "azurerm" {
  features {}
}

# Create a resource group
resource "azurerm_resource_group" "cr-resource-group-1" {
  name     = "cr-resources-group-1"
  location = "Central India"

  tags = {
    environment = "cr-test"
  }
}

resource "azurerm_container_registry" "cracr1" {
  name                = "cracr1"
  resource_group_name = azurerm_resource_group.cr-resource-group-1.name
  location            = azurerm_resource_group.cr-resource-group-1.location
  sku                 = "Basic"
  admin_enabled       = false
}