variable "ingress_namespace" {
  type = string
}

variable "cert_manager_namespace" {
  type = string
}

variable "external_secrets_namespace" {
  type = string
}

variable "observability_namespace" {
  type = string
}

variable "common_labels" {
  type    = map(string)
  default = {}
}
