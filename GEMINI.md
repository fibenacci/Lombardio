# Gemini Project Context: Lombardio

Lombardio is a modular cloud software platform for pawnshops ("Pfandleiher"). It follows a microservices architecture with a strong focus on tenant isolation, backend-enforced authorization, and auditability.

## Core Mandates & Engineering Standards

All development must strictly adhere to these principles:

- **TDD (Test-Driven Development):** Always create a reproducing or verifying test *before* making any code changes. No implementation without a corresponding test case.
- **KISS (Keep It Simple, Stupid):** Prioritize simplicity. Avoid over-engineering and speculative features. Implement the most straightforward solution that fulfills the requirement.
- **Clean Code & SOLID:** Adhere to Clean Code principles and the SOLID architectural standards. Ensure high cohesion, low coupling, and clear responsibilities within the domain.
- **Hexagonal Architecture:** Maintain a strict separation between domain logic (ports) and infrastructure (adapters).

For detailed product direction and service boundaries, refer to:
- [AGENTS.md](./AGENTS.md) - AI Agent Guidelines and Repository Expectations.
- [concept.md](./concept.md) - Product Strategy, Sales Modes, and Domain Model.

## Project Overview

- **Core Purpose:** Legally compliant handling of customer master data, pledged item inventory, loan/contract workflows, and auctions.
- **Architecture:** Microservices-based backend with an internal admin frontend.
- **Tech Stack:**
    - **Backend:** Spring Boot (Java 21) for business-critical services, Go for lean infrastructure/high-throughput services.
    - **Frontend:** Vue.js 3 (Vite, Pinia, PrimeVue).
    - **Infrastructure:** Traefik (API Gateway), Keycloak (IAM), PostgreSQL (Database), RabbitMQ (Message Broker), Redis (Cache).
    - **Local Runtime:** Docker Compose.
    - **Deployment:** Kubernetes, Kustomize, Terraform.

## Repository Structure

- `services/`: Backend microservices.
    - `platform`: Tenant and feature management.
    - `platform-security`: Shared security module for Java services.
    - `identity-intelligence`: Unified domain for Identity, KYC, and AML (replaces customer, kyc, aml).
    - `pawn-ticket`: Pawn ticket issuance and financial calculations.
    - `loan-origination`: Loan case creation and checks.
    - `identity-access`: (Managed via Keycloak in local dev).
    - `integration`: Go-based event consumer/forwarder.
    - `reporting`, `auction`, `online-auction`: Domain-specific services.
- `frontend/app/`: Vue.js admin back-office application.
- `infra/`: Local and deployment infrastructure assets.
- `docs/`: (Note: high-level docs like `concept.md` and `AGENTS.md` are in the root).

## Building and Running

### Prerequisites
- Docker & Docker Compose
- Java 21 & Maven
- Node.js & npm

### Local Development (Docker Compose)
1. **Setup Environment:**
   ```bash
   cp .env.example .env
   # Edit .env as needed
   ```
2. **Start full stack:**
   ```bash
   docker compose up --build
   ```
3. **Start specific profiles:**
   ```bash
   docker compose --profile ops up --build
   ```
4. **Service Access:**
    - Frontend: `http://localhost:5173`
    - Traefik API Gateway: `http://localhost:8081`
    - Keycloak: `http://localhost:8080`

### Individual Service Commands
- **Java Services (Maven):**
  ```bash
  mvn clean install # From root to build all
  mvn spring-boot:run -f services/platform/pom.xml
  ```
- **Frontend (npm):**
  ```bash
  cd frontend/app
  npm install
  npm run dev
  ```
- **Go Services:**
  ```bash
  cd services/integration
  go run cmd/integration-service/main.go
  ```

## Development Conventions

### General Principles
- **Tenant Isolation:** Every request must be scoped by `tenantId`. Never allow cross-tenant data access.
- **Authorization:** Enforce server-side permissions for every action. Do not rely on UI-side hiding.
- **Auditability:** Every business-relevant action must generate an audit trail.
- **Service Boundaries:** Keep business logic out of controllers. Use domain ports and infrastructure adapters (Hexagonal/Clean Architecture).

### Coding Style
- **Java:** Follow standard Spring Boot / Clean Code practices. Use Records for DTOs and immutable domain objects where possible.
- **Go:** Follow standard idiomatic Go patterns.
- **Frontend:** Use PrimeVue components. Favor operational transparency and clear error states over decorative elements.

### Testing
- **Mandatory TDD:** Red -> Green -> Refactor.
- **Backend:** Unit tests for domain logic, integration tests for persistence and security.
- **Frontend:** Component tests for forms and stateful logic.

## AI Agent Guidelines (from AGENTS.md)
- **Focus:** Maintain focus on the secure administration core (Auth, Tenants, Users, Roles).
- **Scope:** Do not expand into complex domain logic (pledge lifecycle, etc.) unless explicitly requested.
- **Security:** Always use conservative security defaults (e.g., UTC for timestamps, Argon2id/bcrypt for hashing).
- **Audit:** Records actor, target, action, and timestamp for all security-relevant events.
