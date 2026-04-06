# Lombardio Architecture Wiki (arc42)

This document describes the software architecture of the cloud platform **Lombardio** for the trade and lending of tangible assets.

---

## 1. Introduction and Goals

### 1.1 Problem Statement
Lombardio is a modular **open-source platform** (planned) for pawnshops, jewelers, and high-end second-hand trade. The goal is the legally compliant, highly automated processing of goods purchases, pawn loans, inventory management, and auctions for the entire EU region.

### 1.2 Quality Goals
1.  **Tenant Isolation:** Absolute separation of data between different companies.
2.  **Auditability:** Complete, tamper-proof logging of all business and compliance-relevant actions.
3.  **Security:** Backend-enforced authorization and identity management (IAM) at a banking level.
4.  **Automation:** Maximum relief for staff through intelligent workflows (AI valuation), hardware connectivity (scales), and digital signatures.
5.  **Multi-Jurisdiction & Compliance:** Dynamic mapping of different legal jurisdictions and industry rules (Pawnshop Ordinance - PfandlV, Trade Regulation - GewO, CCD II).
6.  **Simplicity (KISS):** Modular architecture that allows using only the required functions (e.g., only gold purchase).

### 1.3 Stakeholders
| Role | Expectation |
| :--- | :--- |
| **Merchant / Pawnbroker** | Fast recording, legally secure contracts, automated inventory management. |
| **Jewelers / Buy & Sell** | Correct handling of margin taxation, easy gold value determination. |
| **Platform Operator** | Simple tenant management, scalability, EU-wide usability. |
| **Auditors/Authorities** | Compliance with AMLA (GwG), Cash Register Security Ordinance (KassenSichV), and PawnlV; traceable history. |

---

## 2. Constraints

### 2.1 Technical Constraints
*   **Backend:** Spring Boot (Java 21) for business-critical logic; Go for infrastructure/throughput.
*   **Frontend:** Vue.js 3 (Vite, Pinia, PrimeVue).
*   **Infrastructure:** PostgreSQL, Redis, RabbitMQ, Traefik (API Gateway), Keycloak (IAM).
*   **Deployment:** Docker Compose (Local), Kubernetes/Kustomize, Terraform.

### 2.2 Organizational Constraints
*   **Development Model:** TDD (Test-Driven Development) is mandatory.
*   **Architectural Style:** Hexagonal Architecture (Clean Architecture).

---

## 3. Context
### 3.1 Business Context
Lombardio interacts with:
*   **Customers:** Via an optional Customer Portal.
*   **Sales Channels:** Shopware, eBay, Shopify (planned).
*   **Payment Systems:** PayPal, Wero, bank transfers.
*   **Finance:** DATEV interface for tax advisors and reports to the tax office.
*   **Compliance:** Cloud-TSE (Technical Security System) for KassenSichV compliance.

---

### 3.2 Technical Context
The system runs as a microservice landscape in Kubernetes. Communication takes place synchronously via REST/OpenAPI and asynchronously via Domain Events (RabbitMQ). External connections (e.g., DATEV export) are implemented via dedicated integration adapters.

---

## 4. Solution Strategy
1.  **Microservices:** Division into Bounded Contexts (Platform, Identity, Pawn-Ticket, Buy-In, etc.).
2.  **Modular Domains:** Clear separation between pawn-based processes and purely trade-based processes (Buy-In).
3.  **Domain-Driven Design (DDD):** Focus on commonalities in asset management (valuation, identity, inventory).
4.  **Events:** Use of the Outbox pattern for consistent data storage and audit logs.
5.  **Multi-Jurisdiction Policy Engine:** Centralized management of country-specific compliance rules.

### 4.1 Hexagonal Architecture Hardening
To keep the described hexagonal vision more than a wish, the following strict guardrails apply:
1.  **Dependency rules:** Domain models may not import Spring/JPA/Vue/Keycloak types nor call `fetch`/`axios` directly. Application use cases may only reference domain modules and declared ports. Infrastructure adapters implement ports and glue together the actual frameworks.
2.  **Ports at the edge:** Keycloak, persistence, messaging, external services, schedulers, clock, and UUIDs are modeled as ports. Adapters implement them and live solely in the infrastructure or BFF layer.
3.  **DTO/Entity boundary:** Requests/responses, JPA entities, API mapping, and DTOs remain in adapters/API layers and never leak into the domain. Mappers live right at the adapter boundary.
4.  **Explicit use cases:** Every business flow (`CreateTenant`, `IssuePawnTicket`, `InviteCustomerPortalUser`, etc.) has defined input/output DTOs and explicit ports for tenant/auth context. Policy decisions stay transparent and testable inside the use case.
5.  **Tenant/auth context as parameters:** `tenantId`, `actorId`, permissions, transaction IDs, and timestamps are use-case inputs or provided via explicit context ports rather than global static access.
6.  **Cross-service contracts:** Communication across contexts happens only via defined APIs, events, or dedicated client ports; no internal domain models are re-exported.
7.  **Frontend adapter hardness:** Frontend modules call only their own application services, which in turn speak to ports; shared adapters do not allow direct `fetch` calls between modules.
8.  **Architecture tests:** ArchUnit rules (backend) and Dep-Cruiser/ESLint rules (frontend) guard import and layer violations. They fail the build if a new module breaks the boundary.
9.  **Reference modules:** Harden one service or feature as the benchmark (e.g., `identity-intelligence` or the `customers` frontend) before rolling out the pattern further.

### 4.2 Auction Boundaries and Projections
The split between `services/auction` and `services/online-auction` follows these ownership rules:
1.  **Canonical auction core in `auction`:** The `auction` service is the business source of truth for disposition auctions, lot identity, hammer result, disposition evidence, settlement, and surplus.
2.  **Online channel in `online-auction`:** The `online-auction` service owns realtime sessions, bidder registration, bidder approval, access tokens, countdown logic, minimum increments, and the live bid stream.
3.  **Projection instead of a second truth:** `online-auction` may hold auction and lot data as projections or replicated read models, but not as a second canonical business truth for hammer results, settlement, surplus, or disposition status.
4.  **Explicit contracts:** Synchronization between both services happens through defined APIs, events, or dedicated client ports. Internal domain models are neither shared nor reused directly.
5.  **One channel, not a second bounded context for the same core logic:** Online auctions are a specialized execution channel of the auction domain core, not a replacement for that core's business ownership.

---

## 5. Building Block View

### 5.1 Overall System (Level 1)
*   **`services/platform`:** Management of tenants and features.
*   **`services/identity-intelligence`:** KYC, AML, and identity verification.
*   **`services/pawn-ticket`:** Core logic for pawn loans and calculations.
*   **`services/loan-origination`:** Creation of loan cases.
*   **`services/integration`:** Go-based event consumer for external systems.
*   **`services/reporting`:** Central collection of financial data, dashboard metrics, and DATEV exports.
*   **`services/buy-in`:** (Planned) Independent service for general goods purchasing (jewelers, second-hand trade) including margin taxation (§ 25a UStG).
*   **`frontend/app`:** Central Back-Office interface.

---

## 6. Runtime View

This section describes the central business processes in detail using technical domain terms.

### 6.1 Pawn Loan Lifecycle
This process describes the path from the initial valuation to the resolution of the contract.

1.  **Loan Origination:**
    *   A **customer** presents a piece of **collateral**.
    *   Staff perform an **identity check (KYC/AML check)**.
    *   The collateral is valued and a loan amount is determined.
    *   Upon payout, a legally binding **Pawn Ticket** is created. This is the "anchor" for all further calculations.
2.  **Contract Management:**
    *   During the term, the customer can perform an **extension** by paying the incurred interest and fees.
    *   A **partial payment** reduces the loan amount and leads to the creation of a follow-up pawn ticket.
3.  **Resolution:**
    *   **Redemption:** The customer pays back the loan, interest, and fees and receives their collateral back.
    *   **Expiration:** If the pawn ticket is not redeemed or extended within the grace period, the collateral is considered **expired collateral**.

### 6.2 Disposition Lifecycle (Expired Collateral)
If a pawn is not redeemed, it transitions into the disposition process.

1.  **Disposition Preparation:**
    *   Expired collaterals are transferred into a **disposition case**.
    *   The decision is made here: Does the object go to **auction** or is it released for **private sale**?
    *   After release, the collateral becomes a **sellable item**.
2.  **Processing:**
    *   The sale takes place via the integrated channels.
    *   The system automatically calculates the **surplus** (proceeds minus all claims) for later payout to the customer or transfer to the tax authorities.

### 6.3 Trade Lifecycle: Direct Buy-In & Resale
This process describes the flow of goods without lending (e.g., gold purchase or jeweler trade).

1.  **Buy-In:**
    *   A **customer** offers a tangible asset for direct sale.
    *   **Check:** Identity check (AMLA) and material valuation (AI/scale).
    *   **Completion:** Creation of a buy-in receipt and immediate transfer of ownership to the tenant.
2.  **Resale:**
    *   The article is listed as a **sellable item** in multi-channel distribution.
    *   Upon sale, an invoice is created applying **margin taxation (§ 25a UStG)**.

### 6.4 Auction Preparation & Auction Reporting
The preparation of an auction is subject to strict legal requirements (PfandlV) and requires efficient data provision for auctioneers.

1.  **Legally Secure Public Notice (§ 9 Para. 4 PfandlV):**
    *   The system automatically generates the text for the public announcement.
    *   This contains: location/time, name of the pawnbroker, general description of the pawns (e.g., "gold jewelry"), and the complete list of pawn numbers or number series.
2.  **Auction Catalog (Auctioneer Export):**
    *   A detailed **auction catalog** (PDF/Excel) is provided for internal or external auctioneers.
    *   Content: Lot number, item description, estimated value, minimum bid (limit), and if applicable, internal notes on the condition.
3.  **Proof of Disposition:**
    *   After the auction, a report on the successful bids is created, which serves as the basis for calculating the surplus.
4.  **Online channel as projection:**
    *   When an auction is executed online, `services/online-auction` consumes the relevant auction and lot data as a projection of the canonical auction core.
    *   Realtime bids, bidder sessions, and channel-specific interaction state are managed in the online service.
    *   The authoritative business decisions for hammer result, settlement, surplus, and final disposition status remain in `auction`.

---

## 7. Deployment View

The deployment view describes the technical infrastructure on which the Lombardio platform is operated.

### 7.1 Infrastructure Model
Lombardio is optimized for operation in a cloud-native environment:
*   **Orchestration:** Kubernetes (K8s) serves as the primary platform for container management.
*   **Provisioning:** Infrastructure-as-Code (IaC) via Terraform to manage cloud resources (namespaces, databases, certificates).
*   **Configuration:** Use of Kustomize to control environment-specific overlays (Production, Staging).

### 7.2 Runtime Environments
1.  **Local Development:** Docker Compose for fast emulation of the entire system.
2.  **Staging/Production:** Managed Kubernetes clusters with automatic scaling of business services.

### 7.3 Infrastructure Components
*   **API Gateway:** Traefik handles ingress management, SSL termination, and routing to the microservices.
*   **Identity Management:** Keycloak runs as the central IAM service within the cluster.
*   **Persistence & Messaging:**
    *   PostgreSQL (Master data)
    *   Redis (Caching & session management)
    *   RabbitMQ (Asynchronous event bus communication)

---

## 8. Cross-cutting Concepts

This chapter describes the domain-specific and technical concepts that are relevant across multiple building blocks.

### 8.1 Specifications by Pawn Categories
Different categories of pawn items require specific workflows for storage, valuation, and information gathering to ensure compliance with legal requirements (§ 6, 7, 8, 10 PfandlV) and insurance coverage.

#### 8.1.1 Jewelry & Watches
*   **Legal Mandatory Information:** Weight and fineness stamps (e.g., "14k gold").
*   **Information Gathering:** Fineness, gross/net weight, stone setting (carat, cut), movement functionality (watches).
*   **Weighing:** Mandatory use of a **calibrated precision scale of Class II** (accuracy min. 0.01 g / 0.001 g). Calibration must be renewed every 2 years.
*   **Valuation:** Real-time market value (gold price API integration) vs. resale value.
*   **Storage:** VdS-certified safes.

#### 8.1.2 Electronics
*   **Legal Mandatory Information:** Brand, model, serial number, or IMEI.
*   **Information Gathering:** Optical condition, functional test (battery, display), accessories, confirmation of factory reset (data protection).
*   **Valuation:** Current value considering the high depreciation rate.
*   **Storage:** Dry, temperature-controlled storage; protection against static discharge.

#### 8.1.3 Vehicles
*   **Legal Mandatory Information:** Manufacturer, type, license plate, VIN, engine number, spare tires, payload.
*   **Information Gathering:** Mileage, first registration, accident damage, presence of documents (registration certificate I/II).
*   **Valuation:** Market value determination (DAT/Schwacke) minus storage costs.
*   **Storage (§ 10 PfandlV):** Secure parking space, battery maintenance, regular movement (avoidance of flat spots).

### 8.2 Compliance, Authorities & Problem Customers
The handling of stolen goods and problematic customers is strictly regulated by law (BGB § 935, PfandlV, and AMLA/GwG).

#### 8.2.1 Handling Stolen Goods
*   **No Good Faith Acquisition:** A pawn right cannot be acquired on stolen items (§ 935 BGB). Identified stolen goods must be returned without compensation to the owner or the police.
*   **Prevention:** Comparison of serial numbers (electronics, bicycles, tools) with police search lists during recording.
*   **System Note:** Pawn items can be marked as "POLICE_INQUIRY" or "STOLEN_CONFIRMED" in the system, which immediately blocks further disposition (extension/auction).

#### 8.2.2 Problem Customers & Blacklisting
*   **Internal Blocklist:** Customers who have already stood out due to stolen goods, fraud, or aggressive behavior are marked with a `BLOCK` status in the `identity-intelligence` service.
*   **Warning Signals:** Missing accessories (charging cable for phones), missing proof of ownership for high-value goods, or contradictory statements about origin.
*   **Anti-Money Laundering (AML):** Creation of suspicious activity reports (SARs) for unusual transaction patterns via the FIU's goAML interface.

#### 8.2.3 Cooperation with Authorities
*   **Duty to Provide Information:** According to PfandlV, business books (in Lombardio: digital audit logs and pawn registers) must be presented to authorities upon request for inspection.
*   **Seizure:** The system documents official seizures without gaps to prepare civil law recovery claims against the pledgor.
*   **Anti-Tipping-Off:** In the case of ongoing money laundering suspicious activity reports, the customer must not be informed about the report according to AMLA. The system must suppress corresponding status displays for the customer (portal).

### 8.3 Platform Administration & Multi-tenancy
Lombardio is designed as a multi-tenant platform. Tenant management takes place via a dedicated Platform Administration view, giving the operator full control over the ecosystem.

#### 8.3.1 Tenant Provisioning
The Platform Administrator is responsible for onboarding new companies:
*   **Tenant Lifecycle:** Creation, suspension (deactivation), and archiving of tenants.
*   **Key Management:** Assignment of unique technical identifiers used for tenant isolation at the database level.
*   **Jurisdiction Assignment:** Definition of the legal framework (e.g., DE, AT, FR) under which the tenant operates.

#### 8.3.2 Feature Management & Licensing
Lombardio follows a modular approach. The Platform Admin controls the range of functions per tenant:
*   **Module Activation:** Targeted activation of features like `aml-compliance`, `online-auctions`, or `buy-in-service`.
*   **Quotas:** (Planned) Management of limits (e.g., number of employees or maximum pawn volume).
*   **Infrastructure-as-a-Service:** Automatic provision of necessary resources (e.g., Keycloak groups) when creating a tenant.

#### 8.3.3 Global Monitoring & Support
Central tools are available for the operational management of the platform:
*   **Tenant Dashboards:** Overview of the activity and status of all connected companies.
*   **Audit Log Access:** Audit-proof access to system events for error analysis and compliance with Service Level Agreements (SLAs).
*   **Support Access:** (Optional) Possibility for Platform Admins to support tenants with technical problems through impersonation or specific support roles (while maintaining strict data protection requirements).

### 8.4 Intelligent Legal Automation
Lombardio uses software logic to ensure compliance with complex deadlines and fee structures of the Pawnshop Ordinance (PfandlV).

#### 8.4.1 Automated Fee Control (§ 10 PfandlV)
*   **Interest Cap:** The system enforces the legal upper limit of **1% per month**.
*   **Fee Scaling:** Cost compensation is automatically calculated based on the current PfandlV annex (e.g., scaled fees up to €300, free agreement above with adequacy check).
*   **Rounding Logic:** Correct calculation of partial months according to the commercial practices of the industry.

#### 8.4.2 Deadline & Disposition Watchdog (§ 9 PfandlV)
*   **Auction Maturity (Grace Period):** Automatic marking of pawn tickets that may be auctioned at the earliest one month after maturity.
*   **Compulsory Disposition:** Monitoring of the 6-month period for auction after the onset of disposition authorization to minimize liability risks for the tenant.
*   **Grace Times:** System-side blocking of redemptions/extensions on the day of the auction ("knock-down protection").

#### 8.4.3 Automated Surplus Management (§ 11 PfandlV)
*   **Surplus Tracking:** Automatic calculation of the additional proceeds after deduction of loan, interest, and costs.
*   **Automatic Remittance:** Monitoring of the 2-year period (after the year of disposition). If the surplus is not collected, the system generates an export for **remittance to the competent tax office**.

#### 8.4.4 Auditability & Retention (§ 3 PfandlV)
*   **4-Year Period:** Ensuring that all booking vouchers and pawn registers are retained in the system for at least 4 years (special PfandlV period) or 10 years (GoBD).
*   **Immutability:** Use of the Outbox pattern and an event log to prove the immutability of the original bookings.

### 8.5 Future Automation Potential & Advanced Compliance
To further relieve the pawnbroker operationally and increase legal certainty, the following extensions are planned:

#### 8.5.1 Supply & Insurance Reporting (§ 8 PfandlV)
*   **Inventory Reporting:** Automated monthly creation of the summary report for the insurance. The system aggregates the current loan total and checks whether the insured sum (at least 200% of the loan) is still sufficient.
*   **Risk Alerting:** Warning when exceeding safe limits or insurance classes per product group.

#### 8.5.2 Digital Dunning & Customer Loyalty
*   **Expiry Reminder:** Automated sending of SMS or emails 14 days before the end of the disposition period. This reduces the forfeiture rate and increases customer satisfaction.
*   **Digital Pawn Ticket (Hybrid):** While the paper ticket (bearer paper) is physically handed over (§ 6 PfandlV), the system simultaneously provides a digital version in the customer portal, including push notifications for status changes.

#### 8.5.3 Mobile Inventory & Warehouse Optimization
*   **Barcode Audit:** Support for mobile devices for fast execution of annual or spot-check inventories by scanning pawn labels.
*   **Storage Location Assignment:** Intelligent suggestions for storage locations based on category (safe vs. shelf vs. vehicle space) and value.

#### 8.5.4 Automated Valuation (Valuation Assistant)
*   **Real-time Rates:** Integration of APIs for precious metal prices (gold, silver, platinum) for automatic calculation of material value.
*   **Market Value Crawler:** (Planned) Connection to marketplace data (e.g., eBay sold listings) for a more precise current value determination for electronics and watches.

#### 8.5.5 GDPR-Compliant Data Deletion
*   **Retention Management:** Automated anonymization of customer data after the expiration of all legal retention periods (PfandlV: 4 years / GoBD: 10 years) to meet GDPR requirements without manual effort.

### 8.6 Dynamic Policy & Compliance Engine (Multi-Jurisdiction)
To enable expansion into different European markets and to map the new EU Consumer Credit Directive (CCD II), Lombardio implements a dynamic policy engine.

#### 8.6.1 Concept of Jurisdiction
Each tenant is assigned to a specific **jurisdiction** (e.g., `DE`, `AT`, `FR`). This assignment controls which legal rule sets are used for loan calculation, deadlines, and compliance checks.

#### 8.6.2 Policy-as-Code & Rule Engine
Rules are externalized and dynamically loaded (interest caps, fee scales, etc.), enabling fast adaptation to legal changes without a software release.

#### 8.6.3 Europe-wide Compliance Interface
Support for EU Digital Identity Wallet (QES), margin taxation according to Art. 311 VAT Directive, and country-specific withdrawal periods.

#### 8.6.4 Hardware Connectivity & Peripherals (IoT / Edge)
Connection of calibrated scales (Class II), scanners, and printers to automate branch processes.

#### 8.6.5 Cloud-TSE & KassenSichV (Compliance)
Tamper-proof signing of all cash transactions via Cloud-TSE (e.g., Fiskaly) and DSFinV-K export.

#### 8.6.6 Independent Buy-In Service
Modular service for general buying and selling with legally secure documentation (§ 38 GewO) and margin taxation.

#### 8.6.7 AI-powered Valuation & Image Recognition
Automated recognition of luxury goods and condition analysis for valuation and stolen goods prevention.

#### 8.6.8 ESG & Circular Economy
Measurement of ecological impact through life cycle extension (Circular Economy Reporting).

#### 8.6.9 Digital Resilience (DORA)
Meeting EU requirements for operational stability (ICT Risk Management, resilience tests).

#### 8.6.10 Optional Open Banking & Credit Assessment (PSD2 / CCD2)
Connection to Account Information Services (AIS) for economic plausibility checks for high-value transactions.

#### 8.6.11 Accessibility (European Accessibility Act - EAA)
Consistent implementation of WCAG 2.1 Level AA for all customer interfaces from 2025.

#### 8.6.12 Price Indication & Consumer Protection (Omnibus Directive)
Automatic tracking of historical prices for compliance with price indication regulations (PAngV) during discount campaigns.

---

## 12. Glossary (Ubiquitous Language)

To ensure consistent communication between the business department and development, the following terms are used bindingly:

| Term (EN/DE) | Definition |
| :--- | :--- |
| **Tenant (Mandant)** | The top organizational unit. A legally independent company (pawnbroker, jeweler, merchant). |
| **Asset (Sachwert)** | The physical object (jewelry, watch, vehicle) that is either pledged or purchased. |
| **Jurisdiction** | The legal authority (country/region) a tenant is subject to, which determines the ruleset. |
| **Policy Engine** | The component for dynamic evaluation of business rules and legal requirements. |
| **Collateral (Pfandgut)** | An asset deposited as security for a loan. |
| **Pawn Ticket (Pfandschein)** | The central contract document defining loan amount, interest, fees, and deadlines. |
| **Expired Collateral (Verfallenes Pfand)** | Collateral whose contractual period including legal grace period has expired without redemption or extension. |
| **Disposition Case (Verwertungsfall)** | The workflow in which it is decided how an expired pawn is legally utilized (auction vs. sale). |
| **Sellable Item (Verkaufsartikel)** | An object that has gone through the disposition process or was purchased directly and is now for sale. |
| **Channel Listing (Marktplatz-Listing)** | The concrete expression of a sellable item on an external platform (e.g., an eBay offer). |
| **Redemption (Einlösung)** | The repayment of the loan by the customer against the return of the collateral. |
| **Extension (Verlängerung)** | The continuation of the loan contract by paying the costs incurred so far. |
| **Partial Payment (Abschlagszahlung)** | A partial repayment of the loan amount, which lowers the interest burden for the future. |
| **Receipt (Quittung/Beleg)** | Proof of a financial transaction (sale, redemption, extension). |
| **Buy-In Receipt (Ankaufschein)** | The legally secure document about the direct purchase of an asset by the merchant. |
| **QES (Qualifizierte Signatur)** | Electronic signature legally equivalent to a handwritten signature (eIDAS). |
| **DORA** | EU regulation on digital operational resilience in the financial sector. |
| **Margin Taxation (Differenzbesteuerung)** | Special regulation (§ 25a UStG / Art. 311 EU Directive) where only the margin is taxed. |
| **DATEV Export** | Export of booking data and master data for the tax office. |
| **Surplus (Verwertungsüberschuss)** | The amount remaining after disposition after deduction of loan, interest, and costs, which may have to be remitted. |
| **Auction Catalog (Auktionskatalog)** | The structured list of all lots in an auction including descriptions and limit prices. |
| **Auction Notice (Bekanntmachungstext)** | The legally required text for the public announcement of an auction. |

---

## 13. Platform Administration & Multi-tenancy

Lombardio is designed as a multi-tenant platform. Tenant management takes place via a dedicated Platform Administration view, giving the operator full control over the ecosystem.

### 13.1 Tenant Provisioning
The Platform Administrator is responsible for onboarding new companies:
*   **Tenant Lifecycle:** Creation, suspension (deactivation), and archiving of tenants.
*   **Key Management:** Assignment of unique technical identifiers used for tenant isolation at the database level.
*   **Jurisdiction Assignment:** Definition of the legal framework (e.g., DE, AT, FR) under which the tenant operates.

### 13.2 Feature Management & Licensing
Lombardio follows a modular approach. The Platform Admin controls the range of functions per tenant:
*   **Module Activation:** Targeted activation of features like `aml-compliance`, `online-auctions`, or `buy-in-service`.
*   **Quotas:** (Planned) Management of limits (e.g., number of employees or maximum pawn volume).
*   **Infrastructure-as-a-Service:** Automatic provision of necessary resources (e.g., Keycloak groups) when creating a tenant.

### 13.3 Global Monitoring & Support
Central tools are available for the operational management of the platform:
*   **Tenant Dashboards:** Overview of the activity and status of all connected companies.
*   **Audit Log Access:** Audit-proof access to system events for error analysis and compliance with Service Level Agreements (SLAs).
*   **Support Access:** (Optional) Possibility for Platform Admins to support tenants with technical problems through impersonation or specific support roles (while maintaining strict data protection requirements).
