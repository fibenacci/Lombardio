# Terraform Foundations

This directory contains the Terraform layer for Lombardio infrastructure foundations.

Terraform is intentionally scoped to cluster-level and environment-level concerns. It is not the primary deployment mechanism for Lombardio application workloads. Service deployments remain managed through `infra/k8` and Kustomize overlays.

## Scope

Terraform should manage:

- cluster add-ons
- environment-wide namespaces for shared platform services
- secret backend integration
- certificate management foundations
- metrics-server and ingress controller installation

Terraform should not manage:

- Lombardio `Deployment`, `Service`, or `Ingress` resources from `infra/k8`
- day-to-day application rollout changes
- tenant application instances as ad hoc per-tenant Kubernetes resources

## Structure

- `environments/staging`
  Staging foundation entry point
- `environments/production`
  Production foundation entry point
- `environments/hcloud-cluster`
  Hetzner Cloud cluster provisioning entry point
- `modules/platform-namespaces`
  Shared namespaces for add-ons
- `modules/platform-addons`
  Helm-installed cluster add-ons
- `modules/cert-manager-cluster-issuer`
  Optional ACME cluster issuer bootstrap
- `modules/vault-cluster-secret-store`
  Optional Vault-backed External Secrets bootstrap

## Current Design Choice

The current repository already deploys Lombardio services to an existing Kubernetes cluster via kubeconfig and `kubectl apply -k`. This Terraform layer therefore starts from the same assumption:

- the target cluster already exists
- Terraform prepares the cluster foundations around Lombardio
- `infra/k8` continues to deploy Lombardio services

If cluster provisioning is later required for a specific cloud provider, provider-specific modules can be added without changing the application deployment model.

The current staging deployment path provisions the Kubernetes cluster separately from the cluster foundations:

- `environments/hcloud-cluster`
  Creates the Hetzner-hosted Kubernetes cluster and exposes kubeconfig output
- `environments/staging`
  Installs cluster add-ons and shared platform foundations into that cluster

## Secret Backend

The first supported secret backend pattern is Vault through External Secrets.

Terraform can:

- install the External Secrets operator
- create a Kubernetes auth secret for Vault access
- register a `ClusterSecretStore`
- optionally create a cert-manager `ClusterIssuer`

Application manifests should then reference `ExternalSecret` resources instead of storing shared-environment secrets inline.

## VPN And Tenant-Hosted Notes

VPN and private connectivity are intentionally not automated here yet. They are environment- and provider-specific and need an explicit design per deployment model.

The intended usage is:

- `saas-shared`
  Terraform manages shared cluster foundations.
- `saas-dedicated`
  Terraform manages a dedicated cluster foundation or dedicated add-on stack for one customer.
- `tenant-hosted`
  Terraform may be used by the customer or by Lombardio to prepare a supported runtime baseline, but the application deployment still flows through versioned Kubernetes manifests or charts.

## Usage

Example:

```bash
cd infra/terraform/environments/staging
cp terraform.tfvars.example terraform.tfvars
terraform init
terraform plan
terraform apply
```

Before applying:

- ensure the target cluster is reachable through the configured kubeconfig
- adjust namespaces, chart versions, and ingress settings
- provide Vault values only if `enable_vault_cluster_secret_store = true`
- use a remote Terraform backend for repeated CI applies on GitHub-hosted runners; the default local backend is not durable across workflow runs

### Hetzner cluster plan

For a local plan against Hetzner Cloud:

```bash
cd infra/terraform/environments/hcloud-cluster
cp terraform.tfvars.example terraform.tfvars
export TF_VAR_hcloud_token=...
export TF_VAR_ssh_public_key="$(cat ~/.ssh/id_ed25519.pub)"
export TF_VAR_ssh_private_key="$(cat ~/.ssh/id_ed25519)"
terraform init
terraform plan
```

## GitHub Actions Inputs

The deployment workflows expect environment-specific GitHub secrets for Terraform, including:

- `ACME_EMAIL_STAGING` / `ACME_EMAIL_PRODUCTION`
- `VAULT_SERVER_STAGING` / `VAULT_SERVER_PRODUCTION`
- `VAULT_ROLE_STAGING` / `VAULT_ROLE_PRODUCTION`
- `VAULT_TOKEN_STAGING` / `VAULT_TOKEN_PRODUCTION`

For Hetzner-backed staging provisioning:

- `HCLOUD_TOKEN_STAGING`
- `HCLOUD_SSH_PUBLIC_KEY_STAGING`
- `HCLOUD_SSH_PRIVATE_KEY_STAGING`

Optional Hetzner overrides:

- `HCLOUD_CLUSTER_NAME_STAGING`
- `HCLOUD_LOCATION_STAGING`
- `HCLOUD_CONTROL_PLANE_TYPE_STAGING`
- `HCLOUD_WORKER_NODE_TYPE_STAGING`

Optional toggles and overrides:

- `TF_ENABLE_CLUSTER_ISSUER_STAGING`
- `TF_ENABLE_CLUSTER_ISSUER_PRODUCTION`
- `TF_ENABLE_VAULT_CLUSTER_SECRET_STORE_STAGING`
- `TF_ENABLE_VAULT_CLUSTER_SECRET_STORE_PRODUCTION`
- `BASE_DOMAIN_STAGING`
- `BASE_DOMAIN_PRODUCTION`
