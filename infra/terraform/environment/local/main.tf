# ============================================================
# Pledger – Local Environment
# ============================================================
# Entry point for local development using kind.
#
# Usage:
#   cd infra/terraform/environments/local
#   terraform init
#   terraform plan
#   terraform apply
#
# To add a tenant: add one entry to locals.tenants below.
# Run terraform apply again — all resources are created automatically.
# ============================================================

# ── Providers ────────────────────────────────────────────────

provider "kubernetes" {
  config_path    = "~/.kube/config"
  config_context = "kind-pledger-local"
}

provider "helm" {
  kubernetes {
    config_path    = "~/.kube/config"
    config_context = "kind-pledger-local"
  }
}

# ── Locals ───────────────────────────────────────────────────

locals {
  environment  = "local"
  domain       = "pledger.local"

  # ── Add tenants here ───────────────────────────────────────
  # Each entry generates: namespace, quota, secrets, configmap,
  # network policies, and all four service deployments.
  tenants = {
    "pfandhaus-alpha" = {
      name           = "Pfandhaus Alpha GmbH"
      plan           = "professional"
      subdomain      = "alpha"
      db_role        = "tenant_alpha"
      cpu_request    = "100m"
      cpu_limit      = "300m"
      memory_request = "64Mi"
      memory_limit   = "256Mi"
      rate_limit_rpm = 100
    }
    "pfandhaus-beta" = {
      name           = "Pfandhaus Beta KG"
      plan           = "starter"
      subdomain      = "beta"
      db_role        = "tenant_beta"
      cpu_request    = "50m"
      cpu_limit      = "150m"
      memory_request = "32Mi"
      memory_limit   = "128Mi"
      rate_limit_rpm = 20
    }
  }

  common_labels = {
    "app.kubernetes.io/part-of"    = "pledger"
    "app.kubernetes.io/managed-by" = "terraform"
    "pledger/environment"          = local.environment
  }
}

# ── Step 1: Cluster ──────────────────────────────────────────

module "cluster" {
  source = "../../modules/cluster"

  cluster_name     = "pledger-local"
  kind_config_path = "${path.root}/../../../kind/cluster.yaml"
}

# ── Step 2: Namespaces ───────────────────────────────────────

module "namespaces" {
  source        = "../../modules/namespaces"
  depends_on    = [module.cluster]
  tenants       = local.tenants
  common_labels = local.common_labels
}

# ── Step 3: Infrastructure services ─────────────────────────

module "database" {
  source        = "../../modules/database"
  depends_on    = [module.namespaces]
  namespace     = "pledger-infra"
  environment   = local.environment
  storage_class = "standard"
  storage_size  = "5Gi"
  tenants       = local.tenants
  common_labels = local.common_labels
}

module "cache" {
  source        = "../../modules/cache"
  depends_on    = [module.namespaces]
  namespace     = "pledger-infra"
  environment   = local.environment
  storage_class = "standard"
  storage_size  = "1Gi"
  max_memory    = "256mb"
  common_labels = local.common_labels
}

module "messagequeue" {
  source        = "../../modules/messagequeue"
  depends_on    = [module.namespaces]
  namespace     = "pledger-infra"
  environment   = local.environment
  storage_class = "standard"
  storage_size  = "2Gi"
  common_labels = local.common_labels
}

# ── Step 4: Network isolation ────────────────────────────────

module "networking" {
  source        = "../../modules/networking"
  depends_on    = [module.namespaces]
  tenants       = local.tenants
  common_labels = local.common_labels
}

# ── Step 5: Gateway ──────────────────────────────────────────

module "gateway" {
  source        = "../../modules/gateway"
  depends_on    = [module.database, module.cache, module.messagequeue]
  namespace     = "pledger-system"
  environment   = local.environment
  image_tag     = var.image_tag
  replicas      = 1
  tenants       = local.tenants
  common_labels = local.common_labels
  redis_host    = module.cache.service_host
  redis_port    = module.cache.service_port
}

# ── Step 6: Tenant instances ─────────────────────────────────

module "tenant" {
  source   = "../../modules/tenant"
  for_each = local.tenants

  depends_on = [module.gateway, module.networking]

  tenant_id      = each.key
  tenant_config  = each.value
  environment    = local.environment
  image_tag      = var.image_tag
  common_labels  = local.common_labels
  db_host        = module.database.service_host
  db_port        = module.database.service_port
  db_name        = "pledger"
  db_password    = module.database.tenant_passwords[each.key]
  redis_host     = module.cache.service_host
  redis_port     = module.cache.service_port
  redis_password = module.cache.redis_password
  nats_url       = module.messagequeue.nats_url
}

# ── Step 7: Ingress routing ──────────────────────────────────

module "ingress" {
  source        = "../../modules/ingress"
  depends_on    = [module.cluster, module.tenant]
  tenants       = local.tenants
  environment   = local.environment
  domain_suffix = local.domain
  tls_enabled   = false
  common_labels = local.common_labels
}
