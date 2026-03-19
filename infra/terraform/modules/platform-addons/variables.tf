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

variable "enable_ingress_nginx" {
  type    = bool
  default = true
}

variable "enable_cert_manager" {
  type    = bool
  default = true
}

variable "enable_external_secrets" {
  type    = bool
  default = true
}

variable "enable_metrics_server" {
  type    = bool
  default = true
}
