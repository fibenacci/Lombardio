module "kube_hetzner" {
  source  = "mysticaltech/kube-hetzner/hcloud"
  version = ">= 2.11.0"

  hcloud_token = var.hcloud_token

  cluster_name = var.cluster_name
  location     = var.location
  
  # Base K3s configuration
  k3s_version = "v1.30.0+k3s1"

  # Infrastructure
  network_region = "eu-central"
  
  # Nodes
  control_plane_nodepools = [
    {
      name        = "main",
      server_type = var.control_plane_type,
      location    = var.location,
      count       = 1
    }
  ]

  agent_nodepools = [
    {
      name        = "workers",
      server_type = var.worker_node_type,
      location    = var.location,
      count       = 1
    }
  ]

  # SSH Access
  ssh_public_key  = var.ssh_public_key
  ssh_private_key = var.ssh_private_key

  # Cluster Features
  # The module handles CCM and CSI automatically
}

output "kubeconfig" {
  value     = module.kube_hetzner.kubeconfig
  sensitive = true
}

output "cluster_ipv4" {
  value = module.kube_hetzner.control_plane_public_ipv4
}
