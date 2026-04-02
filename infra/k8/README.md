# Lombardio Kubernetes Base

This directory contains a small base deployment layout for Lombardio.

## Included components

- PostgreSQL as a single-replica `StatefulSet`
- PostgreSQL as a single-replica `Deployment` with persistent volume claim
- Prometheus as the metrics collector
- Grafana as the dashboard UI
- `identity-access` as a scalable `Deployment`
- `platform` as a scalable `Deployment`
- `reporting` as a scalable `Deployment`
- `customer`, `loan-origination`, `pawn-ticket`, `kyc`, `aml`, `auction`, and `online-auction` as domain service `Deployment`s
- `regula` as a supporting OCR worker `Deployment`
- `centrifugo` as the realtime broker for online auctions
- `frontend` as a scalable `Deployment`
- a basic `Ingress`
- `kustomization.yaml` as the composition entry point

## Apply locally

Create the local cluster first:

```bash
kind create cluster --config infra/kind/cluster.yaml
```

Then deploy the base manifests:

```bash
kubectl apply -k infra/k8/base
```

The base now assumes that:

- the External Secrets CRDs are installed
- a `ClusterSecretStore` named `vault-backend` exists
- the expected secret paths are available in the configured secret backend

For shared environments, prepare those foundations through Terraform first:

```bash
cd infra/terraform/environments/staging
terraform init
terraform apply
```

The base manifests no longer store runtime credentials inline.

Security-relevant runtime defaults are split deliberately:

- the `base/` manifests keep local-kind-compatible cookie settings
- the `staging/` and `production/` overlays patch operator and customer-portal session cookies to `secure=true`
- frontend capability flags for delegation and TOTP stay `false` until matching backend support exists

## Overlays

Environment-specific overlays live in:

- `infra/k8/overlays/staging`
- `infra/k8/overlays/production`

They are designed to be driven by GitHub Actions with image tag replacement at deploy time.

## Notes

- The PostgreSQL workload here is a bootstrap choice for local and early-stage environments.
- For production, prefer a managed PostgreSQL service instead of running the database inside the application cluster.
- Replace the placeholder image names before applying in shared environments.
- Store kubeconfigs and registry access only in GitHub environment secrets, not in the repository.
- Keep runtime secrets out of committed defaults. For local Docker Compose, copy `.env.example` to `.env` and fill in the values there.
- For shared environments, do not rely on application fallback values for session cookies. Set and review:
  - `APP_OPERATOR_SESSION_COOKIE_SECURE`
  - `APP_OPERATOR_SESSION_COOKIE_SAME_SITE`
  - `APP_OPERATOR_SESSION_COOKIE_MAX_AGE_SECONDS`
  - `APP_OPERATOR_SESSION_ENCRYPTION_KEY`
  - `CUSTOMER_PORTAL_SESSION_COOKIE_SECURE`
  - `CUSTOMER_PORTAL_SESSION_COOKIE_SAME_SITE`
  - `CUSTOMER_PORTAL_SESSION_COOKIE_MAX_AGE_SECONDS`
  - `CUSTOMER_PORTAL_SESSION_TTL_SECONDS`
- The expected Vault paths for Kubernetes runtime secrets currently include:
  - `lombardio/postgres`
  - `lombardio/identity-access`
  - `lombardio/platform`
  - `lombardio/customer`
  - `lombardio/pawn-ticket`
  - `lombardio/kyc`
  - `lombardio/aml`
  - `lombardio/loan-origination`
  - `lombardio/auction`
  - `lombardio/online-auction`
  - `lombardio/grafana`
