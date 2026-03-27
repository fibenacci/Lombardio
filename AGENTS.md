# AGENTS.md

## Purpose

This repository is the foundation for a cloud software platform for pawnshops ("Pfandleiher").
The system must support legally compliant handling of:

- customer master data
- pledged item inventory
- loan and contract workflows
- auditability and traceability
- role-based access control

This file defines how coding agents should work in this repository so implementation decisions stay consistent across services and frontend applications.

## Product Direction

The first implementation focus is intentionally narrow:

- authentication
- tenant management
- feature management
- user management
- role and permission management
- frontend components and flows for login and administration

Do not expand into pledge lifecycle, valuation, contracts, payments, auctions, or legal reporting unless explicitly requested.

## Architecture Principles

- Build the platform as modular microservices with explicit bounded contexts.
- Prefer Spring Boot for business-critical services with complex workflows, validation, security integration, and transactional consistency.
- Use Go for lean infrastructure-facing or high-throughput services where low operational overhead and simple concurrency are a better fit.
- Use Rust only when there is a clear need for memory safety plus predictable high-performance workloads, for example rule engines, document processing, or specialized compliance tooling.
- Keep each service independently deployable, observable, and testable.
- Prefer synchronous HTTP APIs only for simple request/response operations. Use events for cross-service propagation where eventual consistency is acceptable.
- Every externally visible business action must be auditable.

## Repository Expectations

- `services/` contains backend microservices.
- `frontend/` contains the operator-facing web application.
- `infra/` contains local and deployment infrastructure.

When adding new modules, keep naming explicit and domain-oriented.

Recommended initial layout:

- `services/platform` for tenants, feature flags, tenant status, platform-level policy
- `services/identity-access` for authentication, token issuance, sessions, tenant users, tenant roles, permissions
- `services/customer` for customer master data later
- `services/collateral` for pledged item management later
- `frontend/app` for the main web UI in Lynx or Vue

Do not merge platform-level tenant management into tenant-level business modules unless explicitly requested.

## Initial Backend Scope

Agents working on the first milestone should focus on these capabilities:

- login with username/email and password
- tenant CRUD for platform administrators
- feature enablement per tenant
- secure password hashing and credential rotation
- user CRUD for tenant staff accounts
- role CRUD per tenant
- permission model with role assignment
- account activation and deactivation
- session or token invalidation on critical account changes
- audit logging for login attempts and administrative changes

Out of scope for now:

- customer self-service
- tenant billing
- pledge contracts
- inventory workflows
- external integrations
- advanced compliance exports

## Initial Frontend Scope

Frontend work should start with:

- login page
- tenant list view
- tenant create/edit form
- tenant feature management view
- logout flow
- current-user session state
- user list view
- user create/edit form
- role list view
- role create/edit form
- role assignment UI on users
- permission display and editing, even if initially static or backend-driven

The UI should be designed as an internal back-office application:

- fast to use
- keyboard-friendly
- dense but clear
- optimized for auditability and administration

## Security Baseline

Security is not optional in this project. Agents must default to conservative choices.

- Hash passwords with a modern adaptive algorithm such as Argon2id or bcrypt with strong defaults.
- Never store plaintext credentials, reset tokens, or secrets in logs.
- Enforce server-side authorization checks for every administrative action.
- Scope every tenant-facing read/write action by tenant on the server side.
- Treat roles and permissions as backend-enforced policy, not frontend-only presentation.
- Record security-relevant audit events with actor, target, action, and timestamp.
- Design for future MFA, IP/device checks, and tenant separation even if they are not implemented in the first milestone.
- Use UTC internally for timestamps.

## Compliance and Domain Guardrails

This software targets a regulated business domain. Even during early implementation:

- preserve change history for administrative actions
- avoid destructive updates when audit-preserving approaches are possible
- model explicit account states instead of deleting operationally relevant data
- keep personally identifiable information access controlled and traceable
- ensure no tenant can access or infer another tenant's records
- never make assumptions about German legal compliance without marking them as needing legal validation

Agents may propose technical support for compliance, but must not present legal assumptions as settled requirements unless they are explicitly provided by the user.

## Service Design Guidance

- Keep APIs explicit and versionable.
- Publish OpenAPI specs for HTTP services.
- Keep domain logic out of controllers and transport adapters.
- Separate platform concerns from tenant concerns.
- Separate authentication concerns from user profile concerns where practical.
- Prefer relational persistence for identity and authorization data.
- Model permissions as stable machine-readable keys.
- Carry `tenant_id` or equivalent tenant scope explicitly through persistence and domain logic for tenant-bound data.

Suggested permission examples:

- `platform.tenants.read`
- `platform.tenants.write`
- `platform.features.write`
- `users.read`
- `users.write`
- `roles.read`
- `roles.write`
- `permissions.read`
- `security.audit.read`

## Frontend Guidance

- Keep authentication state handling centralized.
- Prefer route guards backed by backend session or token validation.
- Build reusable admin primitives early: table, filter bar, form shell, validation summary, confirmation dialog, audit metadata panel.
- Reflect authorization constraints in the UI, but do not rely on the UI as the security boundary.
- Reflect feature enablement constraints in the UI, but do not rely on the UI as the enforcement boundary.
- Favor clear error states and operational transparency over decorative UI.

If the frontend stack is undecided between Lynx and Vue, default to Vue unless the user explicitly chooses Lynx for active implementation.

## Quality Bar

Every meaningful change should move the repository toward:

- runnable local development setup
- automated tests close to the changed code
- documented API contracts
- explicit configuration
- observable runtime behavior

Minimum expectations by area:

- backend: unit tests for domain logic and integration tests for persistence/security-critical flows
- frontend: component tests for forms and stateful auth flows where practical
- infra: reproducible local startup path for required dependencies

## Delivery Rules For Agents

- Prefer small, reviewable increments over broad scaffolding.
- Before adding new services, check whether the capability belongs in an existing bounded context.
- Document new architectural decisions in `concept.md` once that file exists.
- If `concept.md` does not yet exist and the change introduces a structural decision, create a short note in the task summary so the decision can later be transferred there.
- Do not introduce speculative infrastructure for future services unless it directly supports the current milestone.

## First Milestone Recommendation

For the current phase, agents should optimize for a thin but production-oriented slice:

1. an identity/access service with login, users, roles, permissions, and audit events
2. a platform service with tenants and feature management
3. a frontend admin app with login, tenant management, and user/role management views
4. local infrastructure to run services, database, and frontend together

This gives the project a usable security and administration core before domain-specific pawn workflows are implemented.
