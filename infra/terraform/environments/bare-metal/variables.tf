variable "target_ip" {
  description = "IP address of the bare metal server"
  type        = string
}

variable "ssh_user" {
  description = "SSH user (must have sudo privileges)"
  type        = string
  default     = "root"
}

variable "ssh_private_key" {
  description = "SSH private key for connection"
  type        = string
  sensitive   = true
}

variable "ssh_private_key_path" {
  description = "Path to the SSH private key (needed for local-exec SCP)"
  type        = string
}
