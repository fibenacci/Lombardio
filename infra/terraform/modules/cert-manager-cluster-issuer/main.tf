resource "kubernetes_manifest" "cluster_issuer" {
  manifest = {
    apiVersion = "cert-manager.io/v1"
    kind       = "ClusterIssuer"
    metadata = {
      name = var.name
    }
    spec = {
      acme = {
        email  = var.acme_email
        server = var.acme_server
        privateKeySecretRef = {
          name = var.private_key_name
        }
        solvers = [
          {
            http01 = {
              ingress = {
                class = var.ingress_class
              }
            }
          }
        ]
      }
    }
  }
}
