# Lombardio

Lombardio is a modular software platform for pawnshops. It combines a tenant-aware administration core with domain services for customer data, loan origination, pawn tickets, auctions, KYC, AML, and reporting.

## License

This repository is source-available under the custom terms in [LICENSE](/home/fibenacci/Dokumente/Projekte/Lombardio/LICENSE). It is not Open Source in the OSI sense. In particular, redistribution of the project, compiled artifacts, or substantial protected parts of its codebase and protected application structure is not permitted without prior written permission from the applicable rights holder.

The platform is built around four baseline requirements:

- tenant isolation
- backend-enforced authorization
- auditability of operational actions
- explicit service boundaries

## At A Glance

- architecture: microservice-based backend plus internal admin frontend
- backend stack: Spring Boot services and one Python OCR worker
- frontend stack: operator-facing web application
- local runtime: Docker Compose
- infrastructure assets: Kubernetes, Kind, Terraform, and observability setup

## Current Product Scope

The main delivery priority is the secure administration core:

- authentication and session handling
- tenant management
- feature management
- user, role, and permission management
- internal back-office UI

Additional domain services already exist in the repository and can be developed in parallel, but the administration and security foundation remains the primary focus.

## Architecture Overview

```text
                +----------------------+
                |   Frontend Admin UI  |
                |   frontend/app       |
                +----------+-----------+
                           |
                           v
     +---------------------+----------------------+
     |                Core Services               |
     |                                            |
     |  identity-access   platform                |
     |  auth, users,      tenants, features       |
     |  roles, sessions                             |
     +---------------------+----------------------+
                           |
          +----------------+------------------------------+
          |                |              |               |
          v                v              v               v
   customer         loan-origination   pawn-ticket     reporting
   kyc hooks        aml/kyc checks     documents       read models
                    pawn-ticket link   cash flows
          |
          +----------------+------------------------------+
                           |
                           v
                    auction / online-auction
                    local and realtime auction flows

Supporting services:
- integration
- kyc
- aml
- document-ocr
- centrifugo
- postgres
- prometheus / grafana / alertmanager
```

## Feature Overview

### Administration And Security

#### `identity-access`

- email/password login
- session issuance and invalidation
- current-user lookup for downstream services
- user management
- role management
- permission assignment
- TOTP enrollment and activation support

#### `platform`

- tenant creation and update
- tenant listing
- feature management per tenant
- platform-level tenant administration

### Domain Services

#### `customer`

- customer create and update
- customer search
- customer retrieval
- KYC integration hooks

#### `loan-origination`

- valuation guideline listing
- loan case creation
- customer/KYC/AML checks during origination
- pawn-ticket issuance integration

#### `pawn-ticket`

- pawn-ticket issuance
- settlement and repayment calculations
- cash transaction execution
- pawn-ticket document generation
- pawn-ticket overview endpoints

#### `auction`

- auction creation
- auction announcement
- auction opening and closing
- bid placement
- lot settlement
- surplus case tracking

#### `online-auction`

- online auction administration
- bidder registration
- bidder review workflows
- realtime session/token issuance
- Centrifugo integration

#### `kyc`

- KYC status handling
- document prefill support
- OCR integration

#### `aml`

- AML assessment endpoints for downstream workflows
- tenant-aware policy integration

#### `reporting`

- reporting dashboard aggregation
- read integration with downstream services

#### `integration`

- consumes platform domain events from RabbitMQ
- forwards selected events to configured webhooks
- provides the first reusable external-integration worker

#### `document-ocr`

- OCR support for KYC-related flows

### Frontend

The frontend is an internal back-office application for operators and administrators.

Current UI areas include:

- login
- tenant management
- user and role administration
- permission-aware navigation
- customer, loan, pawn-ticket, auction, online-auction, and reporting views

## Quick Start

### Prerequisites

- Docker
- Docker Compose

### Start In 5 Minutes

1. Copy the example environment file:

```bash
cp .env.example .env
```

2. Fill in the required values in `.env`.

   Optional demo data controls:

```bash
DEMO_DATA_ENABLED=true
DEMO_DATA_SCALE=medium
PLATFORM_DEMO_DATA_SCALE=
IDENTITY_ACCESS_DEMO_DATA_SCALE=
CUSTOMER_DEMO_DATA_SCALE=
```

`DEMO_DATA_SCALE` sets the default volume for all seeded services. Use service-specific overrides like `PLATFORM_DEMO_DATA_SCALE=large` or `CUSTOMER_DEMO_DATA_SCALE=small` when one bounded context needs more or less volume than the global default.

   Optional local runtime tuning:

```bash
JAVA_CORE_TOOL_OPTIONS=
JAVA_DEFAULT_TOOL_OPTIONS=
JAVA_LIGHT_TOOL_OPTIONS=
INTEGRATION_GOMAXPROCS=
INTEGRATION_GOMEMLIMIT=
```

The compose stack ships with conservative CPU and memory limits plus JVM defaults. Override these variables only if one service needs a larger heap or different CPU parallelism locally.

   Global hot reload switch:

```bash
COMPOSE_BUILD_TARGET=runtime
```

Set `COMPOSE_BUILD_TARGET=development` to run the whole stack in development targets with hot reload enabled. Set it back to `runtime` to switch all services back to packaged runtime images.

3. Start the full local stack:

```bash
docker compose up --build
```

   `docker compose up` uses `COMPOSE_PROFILES` from `.env`. The example file enables the full dev stack by default.

   Optional variants:

```bash
./infra/scripts/dev.sh up lean
docker compose --profile ops up --build
docker compose --profile auction up --build
docker compose --profile obs up --build
docker compose --profile aux up --build
docker compose --profile ops --profile auction up --build
./infra/scripts/dev.sh stats
```

The *frontend* container uses Vite's hot-reload server by default. To switch just the frontend back to the production Nginx image, set `FRONTEND_BUILD_TARGET=runtime` when building that service. For example:

```bash
FRONTEND_BUILD_TARGET=runtime docker compose up --no-start frontend
docker compose start frontend
```

The environment variable only affects the frontend build target, so the rest of the stack can keep its current build target unchanged.

4. Open the frontend:

```text
http://localhost:5173
```

## Local Service Endpoints

- frontend: `http://localhost:5173`
- identity-access: `http://localhost:8081`
- platform: `http://localhost:8082`
- loan-origination: `http://localhost:8083`
- customer: `http://localhost:8084`
- pawn-ticket: `http://localhost:8085`
- kyc: `http://localhost:8086`
- document-ocr: `http://localhost:8087`
- aml: `http://localhost:8088`
- auction: `http://localhost:8089`
- online-auction: `http://localhost:8090`
- reporting: `http://localhost:8091`
- integration: `http://localhost:8092`
- rabbitmq management: `http://localhost:15672`
- centrifugo: `http://localhost:8000`
- prometheus: `http://localhost:9090`
- grafana: `http://localhost:3000`
- alertmanager: `http://localhost:9093`

## Repository Layout

```text
services/
  identity-access/
  platform/
  customer/
  loan-origination/
  pawn-ticket/
  auction/
  online-auction/
  integration/
  kyc/
  aml/
  reporting/
  document-ocr/
frontend/
  app/
infra/
  centrifugo/
  k8/
  kind/
  observability/
  scripts/
  terraform/
```

## Configuration Model

- local runtime secrets are not committed
- use `.env.example` as the template for local configuration
- Docker Compose reads runtime values from `.env`
- Kubernetes base manifests contain placeholders and must be overridden before shared-environment use

## Development Principles

- keep business logic out of controllers
- keep transport DTOs out of application services
- prefer domain ports with infrastructure adapters
- add tests close to the changed code
- treat security, tenant isolation, and auditability as baseline requirements

## Status

The repository already contains a broad domain surface, but the main product direction is still a production-oriented administration core with strong security and clear service boundaries.
