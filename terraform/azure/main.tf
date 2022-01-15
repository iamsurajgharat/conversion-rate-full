# Create a resource group
resource "azurerm_resource_group" "rg" {
  name     = var.resource_group_name
  location = var.location

  tags = {
    environment = "cr-test"
  }
}

# create azure container registry
resource "azurerm_container_registry" "acr" {
  name                = var.acr_name
  resource_group_name = azurerm_resource_group.rg.name
  location            = var.location
  sku                 = "Basic"
  admin_enabled       = false
}

# add image to the container registry
resource "null_resource" "build-and-push-image" {
  provisioner "local-exec" {
    command = "az acr build --image conversion-rate-main-service:v12 --registry ${var.acr_name} --file ../../main-service/target/docker/stage/Dockerfile ../../main-service/target/docker/stage"
  }

  depends_on = [azurerm_container_registry.acr]
}

resource "azurerm_kubernetes_cluster" "aks" {
  name                = var.cluster_name
  location            = azurerm_resource_group.rg.location
  resource_group_name = azurerm_resource_group.rg.name
  dns_prefix          = var.cluster_name
  default_node_pool {
    name                = "system"
    node_count          = var.system_node_count
    vm_size             = "Standard_B4ms"
  }

  identity {
    type = "SystemAssigned"
  }
}

resource "azurerm_role_assignment" "role_acrpull" {
  scope                            = azurerm_container_registry.acr.id
  role_definition_name             = "AcrPull"
  principal_id                     = azurerm_kubernetes_cluster.aks.kubelet_identity.0.object_id
  skip_service_principal_aad_check = true
}

# postgresql server
resource "azurerm_postgresql_server" "postgresql" {
  name                = "crpostgresqlserver1"
  location            = azurerm_resource_group.rg.location
  resource_group_name = azurerm_resource_group.rg.name

  sku_name = "B_Gen5_2"

  storage_mb                   = 5120
  backup_retention_days        = 7
  geo_redundant_backup_enabled = false
  auto_grow_enabled            = false

  administrator_login          = var.postgres_username
  administrator_login_password = var.postgres_password
  version                      = "11"
  ssl_enforcement_enabled      = false
}

# Enable access from kubernetes services to the postgresql server
resource "azurerm_postgresql_firewall_rule" "postgresfirewall" {
  name                  = "allow-aks"
  resource_group_name   = azurerm_resource_group.rg.name
  server_name           = azurerm_postgresql_server.postgresql.name
  start_ip_address      = "0.0.0.0"
  end_ip_address        = "0.0.0.0"
}