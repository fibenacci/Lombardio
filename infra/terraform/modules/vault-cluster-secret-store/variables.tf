variable "external_secrets_namespace" {
  type = string
}

variable "secret_store_name" {
  type = string
}

variable "vault_namespace" {
  type    = string
  default = ""
}

variable "vault_server" {
  type = string
}

variable "vault_path" {
  type = string
}

variable "vault_version" {
  type    = string
  default = "v2"
}

variable "vault_auth_mount_path" {
  type    = string
  default = "kubernetes"
}

variable "vault_role" {
  type = string
}

variable "vault_token_secret_name" {
  type = string
}

variable "vault_token_secret_key" {
  type = string
}

variable "vault_token" {
  type      = string
  sensitive = true
}
