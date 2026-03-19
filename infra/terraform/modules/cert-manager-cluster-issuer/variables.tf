variable "name" {
  type = string
}

variable "acme_email" {
  type = string
}

variable "acme_server" {
  type = string
}

variable "private_key_name" {
  type = string
}

variable "ingress_class" {
  type    = string
  default = "nginx"
}
