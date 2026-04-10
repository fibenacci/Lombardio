# 💎 Lombardio

> [!IMPORTANT]
> **Development Status:** Lombardio is currently in the active implementation phase following its architectural and conceptual design. It is **not yet a production-ready product**. This repository serves to demonstrate the current progress, architectural patterns, and technical direction.

**Lombardio** is a modular, high-security cloud platform specifically engineered for modern pawnshops (*Pfandleiher*). It transforms complex regulated workflows—from customer identity and pawn-ticket issuance to realtime auctions and multi-channel sales—into a seamless, auditable, and tenant-aware digital experience.

---

## ✨ Project Highlights

- 🏢 **Multi-Tenant Excellence**: Hardened isolation ensures every request is strictly scoped by `tenantId`.
- 🔐 **Security First**: Backend-enforced authorization powered by **Keycloak IAM** and **Regula** policy-as-code.
- 🏗️ **Microservices Architecture**: A modular ecosystem of **Spring Boot (Java 21)** and **Go** services.
- ⚖️ **Compliance & Auditability**: Every business-critical action generates an immutable audit trail for legal peace of mind.
- 🔄 **Full Lifecycle Coverage**: Integrated handling of KYC/AML, Loan Origination, Pawn Tickets, and Realtime Auctions.
- 🌐 **Omnichannel Ready**: Support for local POS sales and external commerce channels like **Shopware** and **eBay**.
- 📊 **Deep Observability**: Full-stack monitoring with **Prometheus**, **Grafana**, and **Alertmanager**.
- 🛠️ **Developer Experience**: Highly optimized workflow with **Makefile** support, Docker Compose, and a strict **TDD** culture.

---

## 🏛️ Core Mandates

We build according to the **Lombardio Way**:
1. **TDD (Test-Driven Development)**: Red 🔴 -> Green 🟢 -> Refactor 🔵. No feature without a verifying test.
2. **Hexagonal Architecture**: Strict separation between core domain logic and infrastructure adapters.
3. **KISS (Keep It Simple, Stupid)**: We favor straightforward, maintainable solutions over speculative complexity.
4. **SOLID & Clean Code**: High cohesion, low coupling, and readable intent.

---

## 🗺️ Architecture Overview

```text
                      +-----------------------------+
                      |      Frontend Admin UI      | (Vue.js 3 / PrimeVue)
                      |       /frontend/app         |
                      +--------------+--------------+
                                     |
                                     v
                          +-----------------------+
                          |    Traefik Gateway    | (Ingress / TLS)
                          +----------+------------+
                                     |
           +-------------------------+-------------------------+
           |                         |                         |
           v                         v                         v
  +-----------------+      +-----------------------+      +------------+
  | Identity Access |<---->|    Platform (BFF)     |<---->| Monitoring |
  |   (Keycloak)    |      | tenants, users, auth  |      | (Grafana)  |
  +-----------------+      +-----------+-----------+      +------------+
                                     |
           +-------------------------+-------------------------+
           |                         |                         |
           v                         v                         v
  +-----------------------+  +-----------------------+  +--------------+
  | Identity Intelligence |  |   Loan Origination    |  | Pawn Ticket  |
  |  Identity, KYC, AML   |  | cases, checks, values |  | financials   |
  +-----------------------+  +-----------+-----------+  +--------------+
           |                         |                         |
           +-------------------------+-------------------------+
                                     |
           +-------------------------+-------------------------+
           |                         |                         |
           v                         v                         v
  +-----------------------+  +-----------------------+  +--------------+
  |  Auctions & Bidding   |  |      Reporting        |  | Integration  |
  |  Local & Realtime     |  | (Read Models / BI)    |  |  (Go Worker) |
  +-----------+-----------+  +-----------------------+  +--------------+
              |
              v
       [ Message Broker ]
    (RabbitMQ / Centrifugo)
```

### 📚 Further Reading
For a deeper dive into our architectural decisions and future roadmap, refer to:
- [Hexagonal Analysis & Strategy](docs/hexagon-analysis-and-strategy.md) – Detailed look at our port/adapter implementation.
- [Developer Experience Refactoring Plan](docs/developer-experience-refactoring-plan.md) – Our roadmap for optimizing local workflows.
- [Security Audit Report](docs/security-audit-report.md) – Recent findings and hardening measures.

---

## 🧩 Service Map

### 🛡️ Core & Security
- **`identity-intelligence`**: Unified domain for Identity, KYC, and AML assessment.
- **`platform`**: Tenant lifecycle and feature-flag management.
- **`platform-security`**: Shared security library for consistent Java service hardening.

### 💰 Financials & Loans
- **`pawn-ticket`**: The financial engine for issuance, settlement, and document generation.
- **`loan-origination`**: Orchestrates the creation of new loan cases and valuation checks.

### 🔨 Auctions & Sales
- **`auction`**: Local auction management and lot settlement.
- **`online-auction`**: Realtime bidder interaction via Centrifugo.
- **`integration`**: Go-based worker for webhooks and external event forwarding.

### 🧪 Supporting
- **`reporting`**: Aggregated read-models for business intelligence.
- **`regula`**: OCR worker for automated document processing via Regula.

---

## 🚀 Quick Start

### 1️⃣ Prerequisites
- **Docker & Docker Compose**
- **Java 21 & Maven** (for local service development)
- **Node.js 20+** (for frontend development)

### 2️⃣ Up in 2 Minutes
```bash
# 1. Setup environment
cp .env.example .env

# 2. Fire up the stack (full stack)
make up
```

### 3️⃣ Common Commands
| Command | Description |
| :--- | :--- |
| `make up` | Start the full local stack (all profiles) |
| `make lean` | Start the lean core stack (resource efficient) |
| `make debug` | Start in Java Debug mode (JDWP ports 5005-5011) |
| `make down` | Stop all local containers |
| `make reset` | Stop containers and remove volumes (DB Reset) |
| `make test` | Run all backend tests via Maven |
| `make static` | Run static code analysis (SpotBugs) |
| `make fix` | Run all available auto-formatters (Spotless, Go, Frontend) |
| `make help` | Show all available Makefile commands |

### 4️⃣ Local Access
- 🖥️ **Frontend**: [http://localhost:5173](http://localhost:5173)
- 🔑 **Keycloak**: [http://localhost:8080](http://localhost:8080)
- 📈 **Grafana**: [http://localhost:3000](http://localhost:3000)
- 🐰 **RabbitMQ**: [http://localhost:15672](http://localhost:15672)

### 5️⃣ Session Security Defaults

Local development uses relaxed cookie defaults so login and portal flows work on plain `http://localhost`:

- `APP_OPERATOR_SESSION_COOKIE_SECURE=false`
- `APP_OPERATOR_SESSION_COOKIE_SAME_SITE=Lax`
- `CUSTOMER_PORTAL_SESSION_COOKIE_SECURE=false`
- `CUSTOMER_PORTAL_SESSION_COOKIE_SAME_SITE=Lax`

For every shared, staging, or production environment, tighten these explicitly:

- set `APP_OPERATOR_SESSION_COOKIE_SECURE=true`
- set `CUSTOMER_PORTAL_SESSION_COOKIE_SECURE=true`
- keep `SameSite` explicit (`Lax` by default, `Strict` only if the login and invitation flows still work)
- review `APP_OPERATOR_SESSION_COOKIE_MAX_AGE_SECONDS`
- provide a dedicated `APP_OPERATOR_SESSION_ENCRYPTION_KEY`
- review `CUSTOMER_PORTAL_SESSION_COOKIE_MAX_AGE_SECONDS`
- review `CUSTOMER_PORTAL_SESSION_TTL_SECONDS`
- keep `VITE_OPERATOR_DELEGATION_ENABLED=false` and `VITE_OPERATOR_TOTP_ENABLED=false` unless matching backend support is actually implemented

The frontend no longer relies on browser-persistent access tokens for operator or customer-portal reloads. Session continuity is provided through `HttpOnly` session cookies plus backend-controlled refresh/session handling.

For operator sessions, the target architecture is a BFF-oriented server-side boundary. The current migration step already keeps operator tokens in an encrypted server-side session inside `platform` and exposes only an opaque `HttpOnly` session cookie to the browser while the full BFF consolidation is rolled out.

### 6️⃣ Developer Conventions For Operator Flows

For all new or changed operator-facing use cases, the following rules are binding:

- the browser must not assemble `Authorization: Bearer ...` headers for operator APIs
- new operator API flows must be exposed through explicit `platform` facades under `/api/v1/platform/operator/...`
- direct frontend-to-service operator calls are not part of the target architecture
- transport and forwarding mechanics belong in shared BFF support classes, not in ad-hoc controller code
- public auction and customer-portal flows remain separate trust boundaries and should not be folded into the operator BFF by default

### Regula OCR License

For local OCR document prefilling, place your Regula license file at:

`infra/regula/regula.license`

The local Compose stack mounts this file into the `regula` container on startup.
Without it, Regula starts in unlicensed mode and OCR requests return HTTP `403`
with `permission denied. Bad license`.

### 7️⃣ Quality Assurance & Static Analysis

We maintain a high quality bar through automated gates:

- **Java Static Analysis**: Powered by **SpotBugs** with the **FindSecBugs** extension. It analyzes bytecode for logic errors, security vulnerabilities, and concurrency issues.
  ```bash
  # Run analysis on all Java services
  ./mvnw compile spotbugs:check
  ```
- **TypeScript Type Checking**: We use `vue-tsc` for rigorous type safety in the frontend.
  ```bash
  # Run typecheck in frontend directory (cd frontend/app)
  npm run typecheck
  ```
- **Linting**: Consistent style via **ESLint** (Frontend) and **Spotless** (Java/Go).

---

## 🛠️ Tech Stack

| Layer | Technology |
| :--- | :--- |
| **Backend** | Java 21 (Spring Boot 3), Go 1.22 |
| **Frontend** | Vue.js 3, Vite, Pinia, PrimeVue |
| **Database** | PostgreSQL 16, Redis (Cache) |
| **Messaging** | RabbitMQ, Centrifugo (Realtime) |
| **Infrastructure** | Traefik, Keycloak, Docker, Kubernetes, Terraform |
| **Ops** | Prometheus, Grafana, Alertmanager |

---

## 📜 License

This repository is **source-available**. See the [LICENSE](./LICENSE) file for custom terms. Redistribution or commercial use of protected parts is not permitted without prior written permission.

Additional legal and collaboration documents:

- [TRADEMARKS.md](./TRADEMARKS.md) for name, logo, and branding restrictions
- [CONTRIBUTING.md](./CONTRIBUTING.md) for contribution workflow expectations
- [CLA.md](./CLA.md) for contributor-rights policy
- [Corporate-CLA-EN.md](./Corporate-CLA-EN.md) for the English corporate CLA template
- [Corporate-CLA-DE.md](./Corporate-CLA-DE.md) for the German corporate CLA template
- [COMMERCIAL.md](./COMMERCIAL.md) for OEM, partner, reseller, and commercial-use paths
- [NOTICE](./NOTICE) for repository notices

---

## 📈 Status

Lombardio is currently focused on the **Secure Administration Core**. While domain services for pawn and auctions are active, we are prioritizing the robustness of identity, tenant isolation, and auditability.

> *Built with ❤️ for the Pawn Industry.*
