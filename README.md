# 💎 Lombardio

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
- 🛠️ **Developer Experience**: One-command local setup with Docker Compose and a strict **TDD** culture.

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
                +----------------------+
                |   Frontend Admin UI  | (Vue.js 3 + PrimeVue)
                |   /frontend/app      |
                +----------+-----------+
                           |
                    [ Traefik Gateway ]
                           |
     +---------------------+----------------------+
     |                Core Services               |
     |  identity-access (Keycloak) | platform     |
     |  auth, users, roles         | tenants      |
     +---------------------+----------------------+
                           |
          +----------------+------------------------------+
          |                |              |               |
          v                v              v               v
   [ Identity ]      [ Origination ] [ Pawn Ticket ] [ Reporting ]
   Identity Intel    Loan Case       Documents       Read Models
   KYC / AML         Calculations    Cash Flows      Dashboards
          |
          +----------------+------------------------------+
                           |
                           v
              [ Auctions & Realtime ]
              Local & Online Auction Flows
              (Centrifugo / RabbitMQ)
```

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
- **`document-ocr`**: Python-based worker for automated document processing.

---

## 🚀 Quick Start

### 1️⃣ Prerequisites
- **Docker & Docker Compose**
- **Java 21 & Maven** (for local service runs)
- **Node.js 20+** (for frontend development)

### 2️⃣ Up in 5 Minutes
```bash
# 1. Setup environment
cp .env.example .env

# 2. Fire up the stack
docker compose up --build
```

### 3️⃣ Local Access
- 🖥️ **Frontend**: [http://localhost:5173](http://localhost:5173)
- 🔑 **Keycloak**: [http://localhost:8080](http://localhost:8080)
- 📈 **Grafana**: [http://localhost:3000](http://localhost:3000)
- 🐰 **RabbitMQ**: [http://localhost:15672](http://localhost:15672)

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

---

## 📈 Status

Lombardio is currently focused on the **Secure Administration Core**. While domain services for pawn and auctions are active, we are prioritizing the robustness of identity, tenant isolation, and auditability.

> *Built with ❤️ for the Pawn Industry.*
