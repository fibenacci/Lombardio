# Bare Metal / On-Premise Cluster Foundation

This environment adapter is for physical servers or existing VMs (not managed by Cloud APIs).

## Requirements
- Ubuntu/Debian Linux (Standard)
- SSH root/sudo access
- Static IP Address(es)

## Implementation Strategy
To maintain flexibility, we use **k3sup** or a simple Terraform `null_resource` with SSH:
1. Connect via SSH to the target machine.
2. Install K3s (lightweight Kubernetes).
3. Use `local-path` storage provisioner (stores data on the actual server disk).

## Data Sovereignty (Tenant-Hosted)
If a tenant wants to host their own data:
- Deploy this entire stack on their local hardware.
- Configure the database to use a local volume (already the default in `infra/k8/base`).
- The data never leaves the tenant's building.
