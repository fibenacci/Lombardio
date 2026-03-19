output "namespaces" {
  value = [for namespace in kubernetes_namespace.platform : namespace.metadata[0].name]
}
