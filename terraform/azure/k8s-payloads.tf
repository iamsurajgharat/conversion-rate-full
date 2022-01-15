resource "kubernetes_deployment_v1" "cr-main-service-deployment" {
    metadata {
        name = "cr-main-service-deployment"
    }

    spec {
        replicas = 1
        selector {
            match_labels = {
                component = "cr-main-service"
            }
        }

        template {
            metadata {
              labels = {
                  component = "cr-main-service"
              }
            }

            spec {
                container {
                  image = "${azurerm_container_registry.acr.login_server}/conversion-rate-main-service:v12"
                  name = "cr-main-service"
                  env {
                    name = "APPLICATION_SECRET"
                    value = "${var.app_secret}"
                  }

                  env {
                    name = "DB_HOST"
                    value = "${azurerm_postgresql_server.postgresql.fqdn}"
                  }

                  env {
                    name = "DB_PORT"
                    value = "5432"
                  }

                  env {
                    name = "DB_NAME"
                    value = "postgres"
                  }

                  env {
                    name = "DB_USER"
                    value = "${var.postgres_username}@${azurerm_postgresql_server.postgresql.name}"
                  }

                  env {
                    name = "DB_PASSWORD"
                    value = "${var.postgres_password}"
                  }
                }
            }
        }
    }
}

resource "kubernetes_service_v1" "loadbalancer" {
  metadata {
    name = "main-app-load-balancer"
  }
  spec {
    selector = {
      component = "cr-main-service"
    }
    
    port {
      port        = 80
      target_port = 9000
    }

    type = "LoadBalancer"
  }
}