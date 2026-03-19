variable "environment" {
  description = "Environment name."
  type        = string
  default     = "staging"
}

variable "cluster_name" {
  description = "Human-readable cluster name for labels and outputs."
  type        = string
  default     = "lombardio-staging"
}

variable "kubeconfig_path" {
  description = "Path to kubeconfig used by the Kubernetes and Helm providers."
  type        = string
  default     = "~/.kube/config"
}

variable "kubeconfig_context" {
  description = "Kubeconfig context name for the target cluster."
  type        = string
}

variable "base_domain" {
  description = "Base DNS domain used by ingress and certificate automation."
  type        = string
  default     = "staging.lombardio.example"
}

variable "ingress_namespace" {
  description = "Namespace for ingress-nginx."
  type        = string
  default     = "ingress-nginx"
}

variable "cert_manager_namespace" {
  description = "Namespace for cert-manager."
  type        = string
  default     = "cert-manager"
}

variable "external_secrets_namespace" {
  description = "Namespace for External Secrets."
  type        = string
  default     = "external-secrets"
}

variable "observability_namespace" {
  description = "Namespace for cluster-level observability add-ons."
  type        = string
  default     = "observability-system"
}

variable "enable_ingress_nginx" {
  description = "Whether ingress-nginx should be installed."
  type        = bool
  default     = true
}

variable "enable_cert_manager" {
  description = "Whether cert-manager should be installed."
  type        = bool
  default     = true
}

variable "enable_external_secrets" {
  description = "Whether External Secrets should be installed."
  type        = bool
  default     = true
}

variable "enable_metrics_server" {
  description = "Whether metrics-server should be installed."
  type        = bool
  default     = true
}

variable "enable_cluster_issuer" {
  description = "Whether a cert-manager ClusterIssuer should be created."
  type        = bool
  default     = false
}

variable "cluster_issuer_name" {
  description = "ClusterIssuer resource name."
  type        = string
  default     = "letsencrypt-staging"
}

variable "cluster_issuer_email" {
  description = "ACME email address used by cert-manager."
  type        = string
  default     = ""
}

variable "cluster_issuer_server" {
  description = "ACME directory URL used by cert-manager."
  type        = string
  default     = "https://acme-staging-v02.api.letsencrypt.org/directory"
}

variable "enable_vault_cluster_secret_store" {
  description = "Whether a Vault-backed ClusterSecretStore should be created."
  type        = bool
  default     = false
}

variable "vault_secret_store_name" {
  description = "External Secrets ClusterSecretStore name."
  type        = string
  default     = "vault-backend"
}

variable "vault_namespace" {
  description = "Vault namespace if Vault Enterprise namespaces are used."
  type        = string
  default     = ""
}

variable "vault_server" {
  description = "Vault server URL."
  type        = string
  default     = ""
}

variable "vault_path" {
  description = "Vault KV path used by External Secrets."
  type        = string
  default     = "kv"
}

variable "vault_version" {
  description = "Vault KV engine version."
  type        = string
  default     = "v2"
}

variable "vault_auth_mount_path" {
  description = "Vault auth mount path for Kubernetes auth."
  type        = string
  default     = "kubernetes"
}

variable "vault_role" {
  description = "Vault role used by External Secrets."
  type        = string
  default     = ""
}

variable "vault_token_secret_name" {
  description = "Name of the Kubernetes secret containing the Vault token or bootstrap token."
  type        = string
  default     = "vault-bootstrap-token"
}

variable "vault_token_secret_key" {
  description = "Key inside the Kubernetes secret that contains the Vault token."
  type        = string
  default     = "token"
}

variable "vault_token" {
  description = "Vault bootstrap token used to create the auth secret for External Secrets."
  type        = string
  sensitive   = true
  default     = ""
}
