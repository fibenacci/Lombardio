output "cluster_name" {
  description = "Target cluster name."
  value       = var.cluster_name
}

output "base_domain" {
  description = "Base domain configured for the environment."
  value       = var.base_domain
}

output "addon_namespaces" {
  description = "Cluster add-on namespaces managed by Terraform."
  value = {
    ingress          = var.ingress_namespace
    cert_manager     = var.cert_manager_namespace
    external_secrets = var.external_secrets_namespace
    observability    = var.observability_namespace
  }
}

output "cluster_secret_store_name" {
  description = "Vault-backed ClusterSecretStore name when enabled."
  value       = var.enable_vault_cluster_secret_store ? var.vault_secret_store_name : null
}
