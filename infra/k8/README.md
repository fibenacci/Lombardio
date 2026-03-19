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
- `document-ocr` as a supporting OCR worker `Deployment`
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

Before that, replace every `change-me` placeholder in `infra/k8/base` or override the secrets in an environment-specific overlay. Do not commit real shared-environment credentials into these base manifests.

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
