variable "hcloud_token" {
  description = "Hetzner Cloud API Token"
  type        = string
  sensitive   = true
}

variable "cluster_name" {
  description = "Cluster Name"
  type        = string
  default     = "lombardio"
}

variable "location" {
  description = "Hetzner Data Center Location"
  type        = string
  default     = "nbg1"
}

variable "control_plane_type" {
  description = "Server type for control plane nodes"
  type        = string
  default     = "cpx21"
}

variable "worker_node_type" {
  description = "Server type for worker nodes"
  type        = string
  default     = "cpx21"
}

variable "ssh_public_key" {
  description = "SSH public key to access the nodes"
  type        = string
}

variable "ssh_private_key" {
  description = "SSH private key to access the nodes"
  type        = string
  sensitive   = true
}
