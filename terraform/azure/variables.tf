variable "resource_group_name" {
  type        = string
  description = "RG name in Azure"
}

variable "location" {
  type        = string
  description = "Resources location in Azure"
}

variable "cluster_name" {
  type        = string
  description = "AKS name in Azure"
}

variable "system_node_count" {
  type        = number
  description = "Number of AKS worker nodes"
}

variable "acr_name" {
  type        = string
  description = "ACR name"
}

variable "postgres_username" {
  type        = string
  description = "Postgresql database username"
}

variable "postgres_password" {
  type        = string
  description = "Postgresql database password"
}

variable "app_secret" {
  type        = string
  description = "Play application secret"
}