locals {
  common_labels = {
    "app.kubernetes.io/part-of"    = "lombardio"
    "app.kubernetes.io/managed-by" = "terraform"
    "lombardio.io/environment"     = var.environment
    "lombardio.io/cluster"         = var.cluster_name
  }
}

module "platform_namespaces" {
  source = "../../modules/platform-namespaces"

  ingress_namespace          = var.ingress_namespace
  cert_manager_namespace     = var.cert_manager_namespace
  external_secrets_namespace = var.external_secrets_namespace
  observability_namespace    = var.observability_namespace
  common_labels              = local.common_labels
}

module "platform_addons" {
  source = "../../modules/platform-addons"

  depends_on = [module.platform_namespaces]

  ingress_namespace          = var.ingress_namespace
  cert_manager_namespace     = var.cert_manager_namespace
  external_secrets_namespace = var.external_secrets_namespace
  observability_namespace    = var.observability_namespace
  enable_ingress_nginx       = var.enable_ingress_nginx
  enable_cert_manager        = var.enable_cert_manager
  enable_external_secrets    = var.enable_external_secrets
  enable_metrics_server      = var.enable_metrics_server
}

module "cluster_issuer" {
  count  = var.enable_cluster_issuer ? 1 : 0
  source = "../../modules/cert-manager-cluster-issuer"

  depends_on = [module.platform_addons]

  name              = var.cluster_issuer_name
  acme_email        = var.cluster_issuer_email
  acme_server       = var.cluster_issuer_server
  ingress_class     = "nginx"
  private_key_name  = "${var.cluster_issuer_name}-private-key"
}

module "vault_cluster_secret_store" {
  count  = var.enable_vault_cluster_secret_store ? 1 : 0
  source = "../../modules/vault-cluster-secret-store"

  depends_on = [module.platform_addons]

  external_secrets_namespace = var.external_secrets_namespace
  secret_store_name          = var.vault_secret_store_name
  vault_namespace            = var.vault_namespace
  vault_server               = var.vault_server
  vault_path                 = var.vault_path
  vault_version              = var.vault_version
  vault_auth_mount_path      = var.vault_auth_mount_path
  vault_role                 = var.vault_role
  vault_token_secret_name    = var.vault_token_secret_name
  vault_token_secret_key     = var.vault_token_secret_key
  vault_token                = var.vault_token
}
