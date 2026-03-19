# Infrastructure

## Local Development

The local stack is started with:

```bash
./infra/scripts/dev.sh up
```

This starts:

- PostgreSQL on `localhost:5432`
- Prometheus on `localhost:9090`
- Grafana on `localhost:3000`
- the `platform` service on `localhost:8082`
- the `identity-access` service on `localhost:8081`
- the `loan-origination` service on `localhost:8083`
- the `customer` service on `localhost:8084`
- the `pawn-ticket` service on `localhost:8085`
- the `reporting` service on `localhost:8091`
- the Vue frontend on `localhost:5173`

Useful commands:

```bash
./infra/scripts/dev.sh logs
./infra/scripts/dev.sh ps
./infra/scripts/dev.sh down
./infra/scripts/dev.sh reset
```

## Container Images

Backend image:

- source Dockerfile: `services/identity-access/build/package/Dockerfile`
- runtime target: `runtime`
- development target: `development`

Frontend image:

- source Dockerfile: `frontend/app/build/package/Dockerfile`
- runtime target: `runtime`
- development target: `development`

## Kubernetes Base

The base Kubernetes manifests live in `infra/k8/base`.

They include:

- PostgreSQL
- Prometheus
- Grafana
- platform
- identity-access
- customer
- loan-origination
- pawn-ticket
- frontend
- ingress

Apply them with:

```bash
kubectl apply -k infra/k8/base
```

## Scaling Guidance

- Scale stateless services horizontally via the `Deployment` replica count.
- Keep the database outside the application cluster in production when possible.
- Move secrets out of inline manifests into a managed secret store before shared-environment usage.
- Add dedicated overlays for `local`, `staging`, and `production` instead of modifying the base manifests directly.

## Observability

Local Grafana ships with:

- default credentials `admin` / `admin`
- a provisioned Prometheus datasource
- a first `Lombardio Overview` dashboard for request rate, latency, heap and threads
- an `Identity Access` dashboard for auth traffic, latency and unauthorized spikes
- a `Loan Origination` dashboard for Beleihungsdurchsatz, p95 latency and rejection/error ratios
- a `Pawn Ticket` dashboard for Pfandscheinausstellung and Kassenvorgaenge
- an `Online Auction` dashboard for Registrierungen, Reviews and Gebote
- a tenant reporting API on `localhost:8091` for Dashboard-KPIs zu Kasse und Pfandbestand

Local alerting ships with:

- Prometheus rule evaluation
- Alertmanager on `localhost:9093`
- initial rules for `identity-access` and `loan-origination`
- severity-aware routing to separate warning and critical default receivers
- example receiver overlays in:
  - `infra/observability/alertmanager/slack-receiver.example.yml`
  - `infra/observability/alertmanager/email-receiver.example.yml`

## GitHub Delivery

The repository now separates delivery into:

- `.github/workflows/ci.yml` for tests and image build/push on `main`
- `.github/workflows/deploy-staging.yml` for automatic or manual staging rollout
- `.github/workflows/deploy-production.yml` for manual production rollout
