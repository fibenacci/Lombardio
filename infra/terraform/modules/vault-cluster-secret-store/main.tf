resource "kubernetes_secret_v1" "vault_bootstrap_token" {
  metadata {
    name      = var.vault_token_secret_name
    namespace = var.external_secrets_namespace
  }

  data = {
    (var.vault_token_secret_key) = var.vault_token
  }

  type = "Opaque"
}

resource "kubernetes_manifest" "cluster_secret_store" {
  manifest = {
    apiVersion = "external-secrets.io/v1beta1"
    kind       = "ClusterSecretStore"
    metadata = {
      name = var.secret_store_name
    }
    spec = {
      provider = {
        vault = merge(
          {
            server  = var.vault_server
            path    = var.vault_path
            version = var.vault_version
            auth = {
              tokenSecretRef = {
                name      = kubernetes_secret_v1.vault_bootstrap_token.metadata[0].name
                key       = var.vault_token_secret_key
                namespace = var.external_secrets_namespace
              }
            }
          },
          var.vault_namespace != "" ? { namespace = var.vault_namespace } : {}
        )
      }
    }
  }
}
