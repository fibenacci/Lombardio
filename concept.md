# Lombardio Concept

## Purpose

This document captures the next structural expansion of Lombardio beyond the current administration core and existing pawn workflows. It defines how normal sales, cash sales, disposition of expired collateral, and external commerce channels should later be introduced without breaking tenant isolation, auditability, or bounded service responsibilities.

The intent is to preserve Lombardio as the system of record for regulated pawnshop operations while enabling selected inventory to be sold locally and through external channels such as Shopware, eBay, and optionally Shopify.

## Context

The repository already contains:

- platform administration and identity/access services
- customer, loan origination, pawn-ticket, auction, AML, KYC, and reporting services
- an internal frontend with administration and cashdesk views
- an `integration` service that already provides a first reusable event-consumer/forwarder pattern

The current product focus remains the secure administration and identity foundation. The sales and commerce capabilities described here are a later expansion and should be implemented in small, reviewable slices.

## Product Goal

Lombardio should eventually support three sales modes:

- branch-level POS handling for normal orders and direct cash sales
- controlled disposal and sale of expired collateral
- publication of selected sellable items to external sales channels

The system must support these modes without making any external platform the owner of stock, pricing, or compliance-relevant process state.

## Guiding Principles

- Lombardio remains the internal source of truth for stock, item provenance, pricing approvals, and audit history.
- External commerce systems are sales channels, not primary business systems.
- Expired collateral must pass through an explicit disposition process before it becomes sellable inventory.
- POS, disposition, and channel publication must be tenant-scoped and backend-authorized.
- Every externally visible business action must remain auditable.
- For unique items, double-sale prevention is mandatory across local and external channels.
- Operator-facing frontend architecture follows a BFF-oriented security model: browser code should not distribute long-lived bearer tokens across business services.
- During migration toward the full BFF target, operator access is kept in an encrypted server-side session at the platform boundary and exposed to the browser only as an opaque `HttpOnly` cookie.

## Scope Boundaries

### In Scope For This Concept

- POS and normal sales handling
- cash sales and simple branch orders
- sellable inventory derived from regular stock or expired collateral
- publication to external channels
- import and reconciliation of externally created orders
- inventory reservation and stock synchronization for unique items

### Out Of Scope For This Concept

- customer self-service storefronts owned by Lombardio
- complex ERP, accounting, or billing replacement
- speculative marketplace coverage without a clear business case
- legal interpretation of German disposition requirements without explicit legal input

## Customer Self-Service Extension

The pawn-specific customer self-service requested for digital pawn tickets should be treated as a distinct bounded capability, not as a side effect of the internal admin UI.

### Product Goal

Customers should be able to:

- view their active pawn tickets and current contract status
- download a digital pawn ticket or contract representation
- request or trigger extension and partial-payment flows
- submit repayment intents through supported payment providers
- see transfer instructions and tenant-side booking status for bank transfers

### Recommended Service Direction

Start with a thin dedicated service:

- `services/customer-portal`

This service should own:

- customer invitation acceptance
- email verification and password setup
- customer-facing sessions
- customer-facing pawn-ticket overview aggregation
- self-service action requests for extension, partial payment, and redemption intents

It should not own the financial business rules for pawn tickets. Those remain in `services/pawn-ticket`.

### Responsibility Split

#### `services/customer`

- stores customer contact data and consent-related master data
- stores whether digital pawn-ticket access was offered and requested
- remains the source of customer identity linkage to tenant and pawn tickets

#### `services/identity-access`

- may later be extended for shared password hashing/session primitives
- should not mix tenant staff users and end-customer identities into the same user model without an explicit separation concept

#### `services/pawn-ticket`

- remains owner of extension, redemption, partial-payment calculations, and contract state
- exposes customer-safe read models and action endpoints for the portal layer

#### `services/customer-portal`

- owns invitation token lifecycle, verification completion, customer login, and customer-facing audit events
- translates customer actions into explicit backend requests against `pawn-ticket`
- never bypasses tenant scope or pawn-ticket policy validation

### Activation Flow

The recommended initial activation flow is:

1. During customer creation or origination, staff may capture an email address and enable digital pawn-ticket access.
2. Lombardio stores the customer opt-in state as invitation pending.
3. The customer receives an email with a one-time verification link.
4. After verification, the customer sets a password and activates portal access.
5. Only after activation may the customer access digital pawn tickets and submit self-service payment intents.

### Payment Flow Guardrails

- Online payment methods such as PayPal, Wero, or other PSPs should produce auditable payment intents, not direct silent state changes.
- Bank transfer support should create a pending payment intent that stays open until a tenant user manually marks it as received.
- Tenant-side booking remains mandatory for manual transfer reconciliation.
- Legal and accounting handling of payment settlement statuses still require explicit validation before production rollout.

## Target Domain Model

The core distinction is between collateral, inventory, sellable items, listings, and completed sales.

### Core Objects

- `CollateralItem`
  Represents the pledged object and its pawn-specific lifecycle.
- `DispositionCase`
  Represents the controlled process that determines whether and how an expired collateral item may be sold.
- `InventoryItem`
  Represents the physically managed stock unit inside Lombardio.
- `SellableItem`
  Represents the internal commercial view of an item prepared for sale.
- `ChannelListing`
  Represents a channel-specific offer derived from a sellable item.
- `Sale`
  Represents the completed commercial transaction, regardless of whether the sale happened locally or externally.
- `Payment`
  Represents payment collection, refund, or cancellation state.
- `Receipt`
  Represents the internal and printable sale record.
- `AuditEvent`
  Represents the immutable audit trail for security- and business-relevant actions.

### Why This Split Matters

An expired pawned item is not automatically a Shopware product or an eBay listing. It must first become an internally approved sellable item. This keeps:

- disposition logic independent from commerce platform specifics
- auditability intact
- pricing and stock ownership inside Lombardio
- future channel additions manageable

## Recommended Service Structure

### Existing Services That Stay In Place

- `services/identity-access`
- `services/platform`
- `services/customer`
- `services/loan-origination`
- `services/pawn-ticket`
- `services/auction`
- `services/online-auction`
- `services/integration`

### New Service Direction

Two implementation strategies are acceptable:

#### Strategy A: Thin First Slice

Introduce one combined service first:

- `services/sales-commerce`

This service initially owns:

- POS sales
- cash sales
- sellable inventory
- channel publication requests
- external order import coordination

This is the preferred starting point because it yields a usable slice faster and avoids premature service splitting.

#### Strategy B: Later Separation

Split the combined service once the domain becomes heavier:

- `services/sales`
- `services/inventory`
- `services/catalog`
- `services/channel-publication`
- `services/collateral`

This separation should happen only when operational or team boundaries justify it.

## Recommended Responsibility Split

### `services/pawn-ticket`

Keeps responsibility for pawn-ticket issuance, redemption, extension, repayment, and current pawn-specific cashdesk operations.

It should not absorb generic retail/POS logic.

### `services/collateral`

Later owns:

- collateral lifecycle
- expiration tracking
- disposition eligibility
- disposition approvals
- transition of collateral into sellable inventory

### `services/sales-commerce`

Owns:

- branch POS
- cash sales
- normal orders
- receipt generation metadata
- discount authorization checks
- stock reservation for sellable items
- channel publication orchestration
- external order intake normalization

### `services/integration`

Should evolve into the first generic outbound/inbound integration worker layer.

It can later host or coordinate adapters for:

- Shopware
- eBay
- Shopify

The business decision logic should still stay in the owning domain service. The integration layer should only execute channel-specific synchronization and transport concerns.

## Sales Modes

### 1. POS / Branch Sales

Use case:

- direct sale at the counter
- bar sale without shipping
- card sale
- quick sale of unique items
- optional customer-linked sale

Expected capabilities:

- item search by inventory number, SKU, barcode, or title
- cart / basket handling
- payment capture
- print-friendly receipt data
- void and cancellation flow with audit trail
- restricted discount handling

### 2. Internal Order Handling

Use case:

- branch reserves an item
- item is prepared for shipment or pickup
- external order is imported and completed internally

Expected capabilities:

- order state tracking
- reservation and release handling
- fulfillment-ready status
- cancellation and refund state tracking

### 3. Disposition Of Expired Collateral

Use case:

- pledged item reaches a state where sale is permitted
- internal staff review and approve valuation and sale preparation
- item becomes sellable inventory

Expected capabilities:

- explicit transition from collateral to disposition case
- internal approval checkpoints
- provenance retention
- price approval and release metadata

## Commerce Channel Model

Lombardio should support a neutral channel abstraction.

### `SalesChannel`

Represents the target sales outlet, for example:

- `POS`
- `SHOPWARE`
- `SHOPIFY`
- `EBAY`

### `ChannelCapability`

Represents technical and business characteristics such as:

- fixed-price sale
- auction support
- stock synchronization
- order import
- image-rich listing
- shipping profile support

### `ChannelListing`

Represents the concrete channel-specific publication record with:

- internal sellable item reference
- tenant reference
- target channel
- remote listing identifier
- listing state
- channel-specific price and availability
- last sync timestamp
- sync error state

This neutral model is required so that eBay does not distort the structure for Shopware or future channels.

## Channel Strategy

### Shopware

Recommended as the first external shop integration.

Reasons:

- better fit for controlled DACH-oriented commerce scenarios
- more flexible product and integration model
- better alignment with tenant-owned data and process control

### eBay

Recommended as the second external channel.

Reasons:

- useful for unique items and liquidation-like inventory
- commercially distinct enough to justify a first-class adapter
- materially different from shop channels because of listing behavior, fees, and marketplace rules

### Shopify

Optional later channel.

Use when:

- a SaaS-hosted web shop is preferred
- speed of rollout matters more than deep customization
- the business case clearly favors Shopify operations

### Future Channels

Potential later candidates:

- Kaufland Marketplace
- OTTO Marketplace
- custom tenant web shops

No channel should be added before the neutral listing and stock model exists.

## Inventory And Stock Ownership

Inventory ownership must remain internal.

Rules:

- unique items must have one internal stock truth
- local POS sales must immediately block external availability
- external sales must immediately block local availability
- temporary reservations must be explicit and expirable
- channel stock updates must be idempotent and retryable

For pawnshop inventory, especially unique jewelry, watches, electronics, and other one-off goods, double-sale prevention is a primary architectural requirement.

## Migration From Existing Systems

Migration from incumbent systems must be treated as a first-class platform capability. New tenants may need to switch from legacy pawnshop software, spreadsheets, local databases, or partially manual processes. This must not be solved by writing directly into production domain tables or by treating each migration as a one-off engineering script.

The goal is to make onboarding feasible, controlled, auditable, and repeatable.

### Migration Principles

- imports must run through a controlled staging area before data is written into operational services
- source-specific extraction must be separated from Lombardio domain validation
- every imported record must be traceable back to its source system and source identifier
- imports must be repeatable in test environments before production cutover
- migration quality must be measured through validation results and reconciliation reports
- security-sensitive data such as passwords and secrets must never be blindly migrated

### Recommended Structural Direction

Two acceptable implementation approaches exist:

#### Strategy A: Start Inside The Integration Layer

Use the existing `services/integration` service as the first host for import orchestration and source adapters.

This is acceptable for the first slices because the repository already contains a reusable integration baseline.

#### Strategy B: Introduce A Dedicated Migration Service

Create:

- `services/migration`

This service later owns:

- import job orchestration
- staging storage
- source adapters
- mapping profiles
- validation reports
- reconciliation reporting

This should happen once migration complexity exceeds what the integration worker should reasonably own.

### Migration Workflow

The recommended end-to-end process is:

1. extract data from the source system
2. load raw records into a staging area
3. normalize source records into Lombardio import records
4. run technical validation
5. run domain validation
6. allow mapping correction and issue resolution
7. run a test import into a non-production tenant or sandbox environment
8. generate a reconciliation report
9. approve and execute production import
10. perform delta import or cutover synchronization if required

This workflow is mandatory for high-risk domains such as active pawn tickets, financial balances, and customer master data.

### Suggested Migration Objects

- `ImportJob`
  Represents a tenant-scoped migration run.
- `ImportBatch`
  Represents a domain-specific subset within a migration run.
- `ImportRecord`
  Represents a single staged source record and its processing state.
- `MappingProfile`
  Represents reusable source-to-target mapping rules.
- `ImportIssue`
  Represents validation errors, warnings, and manual review requirements.
- `ReconciliationReport`
  Represents source-versus-target count and consistency checks.
- `ExternalReference`
  Stores the source system name and source-side identifiers for imported business objects.

### Migration Modes

#### File-Based Import

This should be the first supported migration mode.

Supported source formats may include:

- CSV
- Excel
- JSON
- XML

This mode is the most pragmatic baseline because many incumbent systems can at least export tabular data.

#### Source Adapter Import

Later, dedicated connectors may be added for recurring source systems via:

- database reads
- HTTP APIs
- export bundle parsing

Dedicated adapters should only be implemented after one source system appears often enough to justify productized support.

### Migration Domain Priorities

Recommended import order by business value and risk:

1. tenants
2. users, roles, and permissions
3. customer master data
4. active pawn tickets and related contract state
5. inventory and sellable items
6. open balances and payment-relevant state
7. documents and historical references
8. external listings and channel state only if business-critical

### Identity And Access Migration

Identity data must be migrated conservatively.

Rules:

- user identities may be imported with external references and account states
- password hashes should only be imported if format, security level, and compatibility are explicitly supported
- otherwise migrated users must complete a password reset flow
- legacy roles must never be copied blindly into Lombardio permissions
- role and permission migration must use explicit mapping rules and reviewable outcomes

### Customer And Pawn Data Migration

Customer and pawn data require stronger validation than simple master-data imports.

The import process should preserve:

- source system identifiers
- tenant assignment
- operational status
- timestamps in UTC where possible
- provenance markers indicating the data was migrated rather than created natively

For active pawn tickets and related workflows, the system must validate:

- customer references
- monetary consistency
- status consistency
- due dates and timeline plausibility
- availability of required legal or contractual references where known

### Migration Staging And Validation

The staging layer should not be a loose file drop. It should be queryable and stateful.

Recommended staged record states:

- `RECEIVED`
- `NORMALIZED`
- `VALID`
- `INVALID`
- `REQUIRES_REVIEW`
- `APPROVED`
- `IMPORTED`
- `FAILED`

Validation should happen on two levels:

- technical validation
  Examples: malformed values, missing required fields, duplicate external IDs, date parsing failures.
- domain validation
  Examples: impossible status transitions, unknown tenant references, invalid loan or pawn state, conflicting inventory ownership.

### Reconciliation

Every meaningful migration must end with a reconciliation step.

Minimum reconciliation expectations:

- source record counts versus imported record counts
- list of skipped or rejected records
- unresolved warnings and manual decisions
- sampled monetary consistency checks for financial domains
- cross-reference export for later support investigations

No production cutover should be treated as complete without a reconciliation report.

### Cutover Strategy

Different tenant migrations will require different cutover models.

Supported strategies should include:

- `BIG_BANG`
  Suitable for smaller tenants with short downtime tolerance.
- `STAGED_CUTOVER`
  Suitable when master data can migrate first and operational data later.
- `DELTA_SYNC`
  Suitable when repeated synchronization is needed before final go-live.

For active transactional domains, the migration plan must explicitly define:

- which system is the source of truth at each stage
- the cutoff timestamp
- allowed parallel-run period if any
- rules for late changes during cutover

### Audit And Supportability

Migration actions are operationally sensitive and must be auditable.

The platform should record:

- who uploaded or initiated a migration
- mapping changes and approvals
- import execution timestamps
- affected domains and record counts
- failure and rollback-relevant outcomes

Rollback should not rely on destructive database resets. Instead, migration design should prefer:

- sandbox rehearsals
- dry runs
- explicit import versions
- deactivation or compensation flows where domain data has already been materialized

### Recommended Initial Delivery Path For Migration

#### Milestone A

- support file-based import for tenants, users, customers, and inventory
- introduce staging tables and import job tracking
- add validation and issue reporting

#### Milestone B

- support migration of active pawn tickets and associated contract state
- add reconciliation reports and sandbox rehearsal workflows

#### Milestone C

- add reusable source adapters for recurring incumbent systems
- extend cutover support with delta synchronization where necessary

Migration should be considered a customer-onboarding capability of the platform, not a project-specific exception.

## Infrastructure Provisioning And Deployment Model

Kubernetes and Terraform should both exist in the target architecture, but with explicit responsibilities.

### Recommended Responsibility Split

#### Terraform

Terraform should provision and manage infrastructure foundations such as:

- Kubernetes clusters or managed Kubernetes control planes
- virtual networks, subnets, firewall rules, and load balancers
- managed PostgreSQL
- managed Redis
- object storage for documents, exports, and media
- DNS zones and records
- certificate and secret-management foundations
- cloud IAM, service accounts, and registry access
- selected cluster add-ons installed through Helm when they are platform-level concerns

Terraform should not be the primary mechanism for day-to-day application rollouts of Lombardio services.

#### Kubernetes / Kustomize

The existing Kubernetes manifests should remain the deployment source for:

- Lombardio application workloads
- Deployments, Services, HPAs, and Ingress resources
- environment-specific workload overlays
- application-facing ConfigMaps and secret references
- rollout changes for service versions

This preserves a clean boundary between infrastructure lifecycle and application lifecycle.

### Why This Split Is Preferred

- it avoids managing the same Kubernetes resources in both Terraform and Kustomize
- it keeps application rollout concerns close to the application repository
- it allows infrastructure and application change cadence to differ
- it makes future staging and production environments reproducible without overloading Terraform with app-level drift

### Recommended Terraform Scope For Lombardio

The first useful Terraform scope should be:

- `staging` environment foundation
- `production` environment foundation
- managed database and cache services
- DNS and TLS prerequisites
- secret backend integration
- optional cluster add-ons such as ingress controller, cert-manager, external-dns, and external-secrets

Local development should continue to rely primarily on Docker Compose and optionally `kind`. Terraform should not be a prerequisite for everyday local work.

### Secret Backend Direction

Secret management should not rely on committed Kubernetes manifests or long-lived plaintext environment files for shared environments.

Recommended direction:

- use a dedicated secret backend for staging and production
- inject secrets into workloads through Kubernetes-native integrations rather than manually copying secret values
- separate secret ownership from application deployment ownership

Suitable secret backend categories include:

- cloud-managed secret stores
- HashiCorp Vault or an equivalent platform secret system

Expected secret types include:

- database credentials
- JWT and encryption keys
- marketplace and shop integration credentials
- webhook signing secrets
- SMTP and notification credentials
- object storage credentials

The backend choice should prioritize:

- auditability
- rotation support
- access scoping per environment
- automation compatibility with Kubernetes

### VPN And Private Connectivity

VPNs are not mandatory for every tenant scenario, but they become important in hybrid and tenant-hosted setups.

VPNs are most useful when Lombardio must reach:

- tenant-local databases
- tenant-hosted document stores
- internal line-of-business systems at the customer site
- self-hosted LDAP or directory services
- customer-private reporting or archive systems

Recommended position:

- do not make VPN a universal prerequisite for the core SaaS platform
- support private connectivity as an optional infrastructure capability
- prefer managed site-to-site or private-network connectivity over ad hoc host-level tunnels

VPN access should be governed by:

- tenant-specific routing and isolation
- explicit allowlists
- credential rotation
- connection health monitoring
- audited administrative access

### Cluster Add-On Direction

The following add-ons are strong candidates for Terraform-managed platform foundations:

- ingress controller
- cert-manager
- external-dns
- external-secrets
- metrics-server
- CSI drivers or cloud storage drivers

These are infrastructure concerns rather than domain application concerns.

## Tenant Hosting Models

Lombardio should be designed to support more than one hosting model.

### 1. Platform-Hosted SaaS

Lombardio runs on Lombardio-managed infrastructure and tenants consume it as a service.

Characteristics:

- shared platform operations
- logical tenant isolation in the application
- centralized upgrades and monitoring
- simplest operational model for most customers

This should remain the default hosting model.

### 2. Hybrid Tenant Connectivity

Lombardio runs on the platform, but specific tenant systems or data stores remain inside the tenant environment.

Characteristics:

- application is platform-hosted
- selected integrations access tenant-local systems
- VPN or private connectivity may be required
- useful for gradual migration or regulated data boundaries

This model is often the pragmatic bridge for customers who cannot move all data at once.

### 3. Tenant-Hosted / Customer-Managed

The tenant runs Lombardio or parts of Lombardio inside its own environment.

Characteristics:

- tenant controls infrastructure and local data residency
- platform vendor may still provide images, charts, and upgrade packages
- monitoring, support, and upgrade responsibilities must be explicitly split

This model should be supported only with a clearly defined product boundary. It must not become an unbounded “install anywhere in any shape” commitment.

## Multi-Deployment Strategy

If tenant-hosted operation is supported, Lombardio needs packaging and operational standards beyond the default SaaS shape.

### Recommended Packaging Baseline

- container images as the canonical runtime artifacts
- Kubernetes manifests or Helm charts for tenant-managed deployments
- explicit environment variable and secret contracts
- versioned database migrations
- health checks and observability endpoints in every deployable service

### Recommended Deployment Profiles

At minimum, define these profiles:

- `saas-shared`
- `saas-dedicated`
- `tenant-hosted`

#### `saas-shared`

Multiple tenants share the platform deployment with strict logical isolation.

#### `saas-dedicated`

A single customer receives a dedicated deployment while Lombardio still operates it.

This can be useful for larger customers, stronger isolation needs, or transitional migration phases.

#### `tenant-hosted`

The customer operates the stack in its own infrastructure based on a supported deployment package.

### What Changes When A Tenant Hosts Its Own Data

If a tenant wants to self-host data instead of using the shared platform data layer, the architecture should not fall back to direct database coupling from the SaaS platform.

The recommended options are:

- dedicated Lombardio deployment for that tenant
- hybrid integration through APIs and controlled connectors
- staged migration into the shared platform later if desired

Direct cross-network access from the shared multi-tenant SaaS core into arbitrary tenant-owned production databases should be treated as an exception path, not a normal operating model.

Reasons:

- weakens tenant isolation guarantees
- complicates latency, reliability, and support
- makes schema evolution and compatibility harder
- increases security exposure

### Recommended Support Boundary For Tenant-Hosted Environments

If tenant-hosted is offered, define supported boundaries explicitly:

- supported Kubernetes versions
- supported database versions
- required ingress and certificate setup
- required secret backend or secret injection model
- required observability hooks
- supported VPN/private-connectivity patterns if hybrid integrations exist

Without this boundary, operational support cost will grow faster than product value.

### Data Residency And Control

Tenant-hosted and dedicated deployments can help when customers require:

- local data residency
- stronger contractual separation
- internal network integration
- customer-controlled backup or retention policies

However, the product must still preserve:

- audit logging
- security baselines
- version compatibility
- supported upgrade paths

## Recommended Hosting And Infrastructure Rollout

### Phase 1

- keep local development on Docker Compose and `kind`
- keep Kubernetes/Kustomize as the application deployment path
- rebuild Terraform only for staging infrastructure foundations

### Phase 2

- extend Terraform to production foundations
- introduce a secret backend and Kubernetes secret injection path
- move shared-environment secrets out of committed manifests and ad hoc environment handling

### Phase 3

- add support for dedicated customer deployments
- define VPN/private-connectivity standards for hybrid integrations
- package tenant-hosted deployment artifacts only after operational boundaries are explicit

### Phase 4

- support tenant-hosted customers selectively where business value justifies the operational complexity
- avoid turning custom infrastructure exceptions into undocumented default behavior

## Event Model

The system should use domain events for cross-service propagation and channel synchronization.

Recommended events:

- `collateral.released_for_disposition`
- `disposition.approved`
- `inventory.item.created`
- `sellable_item.created`
- `sellable_item.updated`
- `stock.reserved`
- `stock.released`
- `stock.decremented`
- `listing.requested`
- `listing.published`
- `listing.publish_failed`
- `listing.ended`
- `external_order.imported`
- `external_order.cancelled`
- `sale.completed`
- `sale.cancelled`

Technical expectations:

- use the outbox pattern for reliable event publication
- process channel sync operations idempotently
- store correlation between internal objects and remote identifiers
- support retry and dead-letter handling for failed sync operations

## API Direction

All APIs should remain explicit, versionable, and backend-authorized.

### POS / Sales API

Examples:

- `POST /api/v1/sales`
- `POST /api/v1/sales/{saleId}/payments`
- `POST /api/v1/sales/{saleId}/cancel`
- `GET /api/v1/sales/{saleId}`
- `GET /api/v1/sales`

### Sellable Inventory API

Examples:

- `GET /api/v1/sellable-items`
- `POST /api/v1/sellable-items`
- `PATCH /api/v1/sellable-items/{itemId}`
- `POST /api/v1/sellable-items/{itemId}/reserve`
- `POST /api/v1/sellable-items/{itemId}/release`

### Disposition API

Examples:

- `GET /api/v1/disposition-cases`
- `POST /api/v1/disposition-cases/{caseId}/approve`
- `POST /api/v1/disposition-cases/{caseId}/create-sellable-item`

### Channel Publication API

Examples:

- `GET /api/v1/channel-listings`
- `POST /api/v1/channel-listings`
- `POST /api/v1/channel-listings/{listingId}/publish`
- `POST /api/v1/channel-listings/{listingId}/end`
- `POST /api/v1/channel-listings/{listingId}/sync`

### External Order Intake API

Examples:

- `POST /api/v1/channel-orders/import`
- `GET /api/v1/channel-orders`
- `POST /api/v1/channel-orders/{orderId}/acknowledge`

## Frontend Direction

The frontend should extend the existing internal back-office style and stay dense, keyboard-friendly, and operationally transparent.

### Existing UI Anchor

The repository already contains a `cashdesk` view focused on pawn-ticket settlement flows. That view should remain dedicated to pawn-ticket transactions.

Generic POS and sales should be introduced as a separate UI area instead of mixing retail sales into pawn-ticket redemption screens.

### Recommended New Areas

- `sales-pos`
  For branch sales, quick checkout, and receipt handling.
- `sellable-items`
  For preparing and managing sellable inventory.
- `disposition`
  For reviewing expired collateral and releasing it for sale.
- `channel-listings`
  For channel selection, listing state, and sync troubleshooting.
- `channel-orders`
  For imported external orders and reconciliation.

### POS Screen Requirements

- fast search and selection
- strong keyboard support
- clear stock and reservation state
- discount controls with permission feedback
- payment method selection
- receipt-ready completion state

### Listing Management Requirements

- per-channel visibility
- sync status and error display
- listing lifecycle actions
- image and description completeness checks
- explicit publication approval

### Disposition Workspace Requirements

- provenance visibility from original collateral
- valuation and pricing support
- decision whether item is local-only or multi-channel
- status transitions with actor and timestamp visibility

## Permission Model Additions

Suggested new machine-readable permissions:

- `sales.pos.use`
- `sales.orders.read`
- `sales.orders.write`
- `sales.discount.apply`
- `sales.cancel`
- `inventory.read`
- `inventory.write`
- `disposition.read`
- `disposition.approve`
- `catalog.read`
- `catalog.write`
- `channels.read`
- `channels.publish`
- `channels.unpublish`
- `channels.sync.manage`
- `marketplaces.ebay.manage`

All permission checks must be enforced server-side.

## Audit Expectations

The following actions must generate auditable records:

- price changes
- manual discount application
- cancellation and void actions
- disposition approval
- creation of sellable items from collateral
- listing publication and ending
- external order import and reconciliation
- stock correction or manual reservation release

Audit records should always retain:

- tenant
- actor
- target object
- action type
- timestamp in UTC
- relevant before/after context where appropriate

## Security And Compliance Notes

- **Identity and Access Management:** Integrate **Keycloak** to manage user authentication, authorization, and tenant isolation, ensuring secure access to platform services.
- **Policy Enforcement:** Utilize **Regula** for policy-as-code to enforce security and compliance rules within the Kubernetes environment, such as admission control and resource configuration validation.
- tenant scoping must be enforced for all sales, inventory, disposition, and listing operations
- credentials, tokens, and channel secrets must never be exposed in logs
- personally identifiable customer context in sales flows must remain access-controlled
- disposition and sale of expired collateral may involve jurisdiction-specific rules and requires legal validation before implementation is treated as compliant

## Recommended Delivery Sequence

### Milestone 1: POS Foundation

- introduce `services/sales-commerce`
- implement generic POS sales and cash sales
- add a dedicated POS frontend area
- keep stock ownership internal
- add audit events for sale completion and cancellation

### Milestone 2: Sellable Items And Disposition

- model sellable inventory explicitly
- add minimal disposition workflow from expired collateral to sellable item
- expose item preparation and release screens
- preserve provenance and approval metadata

### Milestone 3: Channel Publication Core

- add neutral sales channel and listing model
- implement publication orchestration and sync job handling
- extend the integration layer for reusable channel adapter execution

### Milestone 4: First External Channels

- implement Shopware adapter first
- implement eBay adapter second
- import external orders into Lombardio for internal completion and stock reconciliation

### Milestone 5: Optional Additional Channels

- evaluate Shopify only if there is a clear business case
- add more marketplaces only after the neutral listing model has proven stable

### Milestone 6: Migration Capability

- introduce controlled import orchestration with staging and validation
- support file-based onboarding of tenants switching from existing systems
- add reconciliation reporting and cutover support for high-risk domains

### Milestone 7: Infrastructure Foundation

- rebuild Terraform around the current Lombardio architecture rather than the unused placeholder layout
- keep Kubernetes/Kustomize as the application deployment mechanism
- introduce secret backend integration for shared environments
- define dedicated and tenant-hosted deployment profiles
- define VPN/private-connectivity standards for hybrid tenant scenarios

## Decision Summary

The preferred future direction is:

- keep `identity-access` and `platform` as the secure administration core
- keep pawn-ticket cashdesk flows separate from generic retail/POS sales
- introduce `sales-commerce` as the first implementation slice for POS and multi-channel sales
- model expired collateral disposition explicitly before creating sellable inventory
- treat migration from incumbent systems as a controlled platform capability with staging, validation, and reconciliation
- rebuild Terraform only as the infrastructure layer beneath Kubernetes, not as a replacement for `infra/k8`
- adopt a proper secret backend for shared environments
- treat VPN/private connectivity as an optional hybrid capability, not a universal dependency
- support `saas-shared`, `saas-dedicated`, and selective `tenant-hosted` deployment models with explicit support boundaries
- treat Shopware, eBay, and Shopify as adapters on top of a neutral internal listing model
- keep stock and audit ownership inside Lombardio

This should be the baseline concept for future work on sales, disposition, and external commerce integration unless a later decision explicitly revises it.

## Demo Data Strategy

For local development and stakeholder walkthroughs, the platform should expose realistic, cross-service demo data instead of tiny ad hoc seed fixtures.

- keep demo data opt-out via `DEMO_DATA_ENABLED`, enabled by default for local compose
- control volume consistently across Spring services via `DEMO_DATA_SCALE=small|medium|large`
- allow per-service overrides via `<SERVICE>_DEMO_DATA_SCALE`, so local walkthroughs can emphasize one bounded context without inflating every dataset
- structure demo seeding per service under a shared pattern with `DemoDataConfiguration`, `DemoDataProperties`, `ReferenceDataSeeder`, and `ScenarioDataSeeder`
- seed deterministic IDs and tenant/customer references so data remains traceable across services
- separate reference data such as permissions, roles, tenants, and feature flags from bulk scenario data
- prefer broad tenant-aware datasets that populate lists, filters, dashboards, KPI widgets, and detail views
- ensure seeders are idempotent so local rebuilds refresh the same demo landscape without drifting identifiers
