locals {
  namespaces = toset([
    var.ingress_namespace,
    var.cert_manager_namespace,
    var.external_secrets_namespace,
    var.observability_namespace
  ])
}

resource "kubernetes_namespace" "platform" {
  for_each = local.namespaces

  metadata {
    name   = each.value
    labels = var.common_labels
  }
}
