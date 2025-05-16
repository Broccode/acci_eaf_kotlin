# ACCI EAF (Axians Competence Center Infrastructure Enterprise Application Framework) Product Requirements Document (PRD)

## Goal, Objective and Context

* **Goal of the EAF:** Internal use to accelerate the development and standardization of enterprise software products provided to external customers. The development with ACCI EAF aims to replace the outdated, no longer extensible, and performance-limited internal "DCA" framework.
* **Focus & Value Proposition:**
  * **For the ACCI Development Team:** Significant time and cost savings in the development of new products and features, improved maintainability and testability through modern technologies and architectures, and an enhanced Developer Experience. "Liberation" from the limitations of the old system.
  * **For End Customers of ACCI Products:** Considerable gain in modern features (e.g., multitenancy, enhanced security, flexible license management, internationalization), improved performance, and a much more modern user experience for products based on the EAF.
* **Context:** The ACCI EAF is developed by the Axians Competence Center Infrastructure team and will be primarily used for enterprise segment software products that run on IBM Power Architecture (ppc64le) at customer sites, often in environments without direct internet access.

## [OPTIONAL: For Simplified PM-to-Development Workflow Only] Core Technical Decisions & Application Structure

This section documents the fundamental technical decisions and the planned application structure for the ACCI EAF.

**1. Core Technology Stack:**

* **Programming Language/Platform:** Kotlin (running on the Java Virtual Machine - JVM)
* **Core Framework (Application Layer):** Spring Boot
* **Architecture Framework (for DDD, CQRS, ES):** Axon Framework
* **Database (primarily for Read Models, State Data, and as Event Store):** PostgreSQL
* **Build Tool:** Gradle

**2. Repository Structure:**

* **Approach:** Monorepo
  * All modules and associated build logic of the ACCI EAF will be managed in a single Git repository.

**3. Application Structure (Modules & Responsibilities):**

The ACCI EAF will be structured as a modular monolith with the following main modules (Gradle sub-projects):

* **`build-logic`**:
  * *Responsibility:* Contains the central build logic, dependency versions (Dependency Management via Gradle Version Catalogs), and conventions for all modules in the monorepo.
* **Framework Modules (Core of ACCI EAF):**
  * **`eaf-core`**:
    * *Responsibility:* Provides the fundamental building blocks, core abstractions (e.g., for Aggregates, Commands, Events), common utilities, and the base configuration for CQRS/ES using the Axon Framework. Forms the foundation for applications utilizing the EAF.
  * **`eaf-iam` (Identity & Access Management)**:
    * *Responsibility:* Implements functionalities for user management, authentication (local and external via LDAP/AD, OIDC, SAML2), and authorization (RBAC, with preparation for ABAC) as a reusable framework module, including support for service accounts.
  * **`eaf-multitenancy`**:
    * *Responsibility:* Provides the logic for multitenancy, including mechanisms for tenant isolation (e.g., via Row-Level Security in PostgreSQL) and for managing the tenant context within the application.
  * **`eaf-licensing`**:
    * *Responsibility:* Offers functionalities for the license management of applications built with the ACCI EAF (e.g., time-limited, hardware-bound, offline/online activation).
  * **`eaf-observability`**:
    * *Responsibility:* Provides standardized configurations and tools for logging (structured), metrics (Prometheus export via Micrometer), health checks (Spring Boot Actuator), and dedicated audit logging.
  * **`eaf-internationalization` (i18n)**:
    * *Responsibility:* Provides tools, conventions, and a base infrastructure for the internationalization and localization of applications, including tenant-specific language and translation management.
  * **`eaf-plugin-system`**:
    * *Responsibility:* Contains the implementation of the plugin infrastructure (based on the Java ServiceLoader API), allowing the EAF and applications based on it to be extended modularly through defined interfaces.
* **Optional/Supplementary Modules:**
  * **`eaf-cli`**:
    * *Responsibility:* Development of command-line interface (CLI) tools for developers using the EAF (e.g., for project scaffolding, code generation, diagnostics).
  * **`app-example-module`**:
    * *Responsibility:* Serves as a reference implementation and quick-start guide. Demonstrates how a typical business application or a specific domain module is developed using ACCI EAF components and best practices.
  * **`eaf-controlplane-api`**: (Backend for the Control Plane UI)
    * *Responsibility:* Provides the RESTful APIs for the Control Plane UI (tenant, user, license, i18n management).
  * **`eaf-license-server`**: (As an EAF-based application)
    * *Responsibility:* Provides the server-side logic for online license activation and validation.

## Functional Requirements (MVP)

The functional requirements for the MVP of the ACCI EAF are structured into 10 Epics. Each Epic with its detailed User Stories and Acceptance Criteria has been previously elaborated and approved. (The full text of the 10 Epics with all revised User Stories and Acceptance Criteria, considering edge cases, error handling, etc., will be inserted or referenced here. For readability in this conversation, only a summary of the Epic titles is provided; the detailed content has already been marked as "approved".)

**Epic Overview:**

1. **Epic 1: EAF Foundational Setup & Core Infrastructure**
2. **Epic 2: Core Multitenancy Implementation**
3. **Epic 3: Core Identity & Access Management (IAM) - Local Users & RBAC**
4. **Epic 4: Control Plane UI - Phase 1 (Tenant & Basic User Management)**
5. **Epic 5: Core Licensing Mechanism**
6. **Epic 6: Internationalization (i18n) Core & Control Plane Integration**
7. **Epic 7: Plugin System Foundation**
8. **Epic 8: Advanced IAM - External Authentication Providers**
9. **Epic 9: Advanced Licensing - Hardware Binding & Online Activation**
10. **Epic 10: EAF Observability & Developer Experience (DX) Enhancements**

*(Note: In the final document, the complete, previously detailed and approved User Stories for each Epic would be included here.)*

## Non Functional Requirements (MVP)

The following Non-Functional Requirements (NFRs) are defined for the MVP of the ACCI EAF:

**1. Performance & Scalability**

* **NFR 1a (Automated Benchmarks):** An automated benchmark suite must be developed and maintained to track the performance evolution of critical EAF operations.
* **NFR 1b (Performance on ppc64le):** The EAF must demonstrate acceptable and consistent performance of its core functions on the ppc64le target platform.
* **NFR 1c (Performance Targets API):** Critical API endpoints of the Control Plane (e.g., tenant creation, user creation, license generation) should have an average response time of **< 500ms** and a P95 response time of **< 1000ms** under a defined baseline load (e.g., 10 concurrent administrative users).
* **NFR 1d (Performance Targets EAF Core):** Core EAF mechanisms used by applications (e.g., tenant context resolution, internal license validation check) should aim for an overhead of **< 10ms** per operation on average on the target hardware.
* **NFR 1e (Performance Targets CQRS/ES):** The EAF must initially be able to process **at least 50 Commands/Events per second** (related to its core CQRS/ES architecture) on the target ppc64le hardware (baseline e.g., 4 vCPUs, 8 GB RAM – TBD).
* **NFR 1f (Scalability Targets Users/Tenants):** The EAF architecture must be designed to support scaling to at least **100 tenants**, each with up to **1,000 users**. MVP testing will use a smaller set (e.g., 5 tenants, 100 users each).
* **NFR 1g (Scalability Targets Event Store):** The Event Store (PostgreSQL in MVP) should initially handle an event volume of at least **1 million events per month** for pilot applications; archival/snapshotting strategies are to be considered conceptually.
* **NFR 1h (Load Test Plan):** An initial load test plan for Control Plane APIs and EAF throughput mechanisms must be defined and executed before the first pilot project goes live.
* **NFR 1i (Load Test Focus):** Load tests will identify bottlenecks, verify response times under load, and determine initial capacity limits.
* **NFR 1j (Stress Test Considerations):** Basic stress tests will be conducted to understand behavior under peak load and resource limits (target: graceful degradation).

**2. Security & Compliance**

* **NFR 2a (Compliance Support):** EAF must support applications in complying with GDPR and ISO 27001.
* **NFR 2b (Certification Preparation):** Architecture must facilitate SOC2 certification.
* **NFR 2c (FIPS Support):** EAF must support the use of FIPS 140-2/3 validated cryptographic modules.
* **NFR 2d (OWASP Top 10):** Active addressing of OWASP Top 10 risks A01-A09.
* **NFR 2e (Threat Modeling):** High-level threat modeling (e.g., STRIDE) for key components during the design phase.
* **NFR 2f (Penetration Testing):** Internal security reviews; external penetration tests planned before wider use or for major releases.

**3. Reliability & Availability (for EAF's own components)**

* **NFR 3a (HA/DR Support for Apps):** EAF must enable the design of HA/DR-capable applications.
* **NFR 3b (Robustness & Restart Behavior):** EAF components must be resilient and allow clean restarts after crashes.
* **NFR 3c (Availability Targets):** Control Plane UI/API and Online License Activation Server should target **99.5% availability during business hours** for MVP.
* **NFR 3d (MTTR):** For critical EAF components, an MTTR of **< 1 hour during business hours** is targeted for MVP. MTBF targets TBD.
* **NFR 3e (Backup & Recovery EAF Services):** Strategies and requirements (RPO/RTO) for backup and recovery of the EAF's own stateful components are **To Be Defined (TBD)**.
* **NFR 3f (Disaster Recovery EAF Services):** A specific DR strategy for the EAF's own hosted components is **To Be Defined (TBD)**.

**4. Maintainability & Extensibility**

* **NFR 4a (Test Coverage):** EAF core modules aim for 100% unit test coverage for critical logic; high coverage (>80%) for new business logic.
* **NFR 4b (Coding Standards):** EAF codebase must adhere to the to-be-created/referenced **"ACCI Kotlin Coding Standards v1.0"**. Adherence checked in CI pipeline.
* **NFR 4c (Updateability in Monorepo):** Updates of EAF modules for applications within the same monorepo should be straightforward.

**5. Usability & Accessibility (for Devs using EAF)**

* **NFR 5a (Comprehensive Developer Documentation):** See Story 10.4.
* **NFR 5b (Documentation Content):** See Story 10.4.
* **NFR 5c (API Design):** EAF APIs and extension points must be optimized for clarity, ease of use, and discoverability.

**6. Offline Capability**

* **NFR 6a (Core Aspects):** EAF must support offline license activation/validation. Deployment of EAF applications in air-gapped environments must be possible.

**7. Auditability / Traceability**

* **NFR 7a (Dedicated Audit Log):** See Story 10.7.
* **NFR 7b (Audit Log Content):** See Story 10.7.
* **NFR 7c (Audit Log Security & Accessibility):** See Story 10.7.

**8. ppc64le-specific requirements**

* **NFR 8a (Optimization for ppc64le):** EAF must be optimized for reliable and efficient execution on ppc64le VMs.

**9. Operational Requirements (Supplementary)**

* **NFR (Configuration Management):** Configurations via externalized files (e.g., `application.yml`) with Spring Profiles. Sensitive data injected via environment variables or securely mounted files (not in Git).
* **NFR (SBOM Generation):** An SBOM (e.g., CycloneDX, SPDX) must be generated for each official EAF release and products based on it. CI/CD pipeline includes an automated step for this.
* **NFR (SBOM Review):** A process for continuous review of SBOMs for licenses and known vulnerabilities of third-party components (e.g., with OWASP Dependency Track) is established.

## User Interaction and Design Goals

This section describes the high-level goals for user interaction and design of the Control Plane UI, intended for managing tenants, licenses, and tenant-specific i18n texts of the ACCI EAF.

* **Overall Vision & Experience:**
  * The user interface should convey a **professional and functional** impression.
  * **React-Admin** serves as a reference and inspiration for style and functionality, implying data-oriented, clear, and efficient operation.
  * The technology preference for the frontend of this Control Plane UI is therefore React.
* **Key Interaction Paradigms:**
  * Tabular views (Data Grids) for lists (e.g., tenants, licenses) with integrated search, filter, and sort functions.
  * Form-based input masks for creating and editing entries.
  * Detail views for displaying all relevant information about a selected entry.
* **Core Screens/Views (Conceptual for MVP):**
  * Login screen for Control Plane administrators.
  * Tenant management view (CRUD operations).
  * License management view (CRUD operations – primarily for the ACCI team).
  * i18n text management view for tenants.
  * User management view (local users) within a tenant (CRUD, status, password reset).
  * RBAC assignment view (assigning roles to users) within a tenant.
  * Service account management view within a tenant (CRUD, credentials, expiration).
  * Authentication provider configuration view for tenants (LDAP, OIDC, SAML).
* **Accessibility Aspirations:**
  * For the MVP, no specific accessibility requirements beyond the standard functionality of the chosen UI framework (e.g., React-Admin) are defined (User preference: "is not necessary").
* **Target Devices/Platforms:**
  * Primary focus: **Desktop web browsers**.
  * Basic responsive display for tablets (viewing core information) is desirable.
* **Branding / Style Guide Constraints:**
  * The Control Plane UI should be designed **neutrally**, without specific ACCI branding elements, to emphasize its functional character.
* **User Feedback & Iteration:**
  * **Initial MVP:** Internal "Dogfooding" & direct feedback from the ACCI team.
  * **Pre-Pilot/Pilot Phase:** Usability tests with "Proxy Users" (internal staff in customer admin roles) or early pilot customers.
  * **Iteration Process:** Collected feedback will be consolidated and prioritized by the PM, and implemented in subsequent development cycles in coordination with the development team.

## Out of Scope / Future Considerations

* **Out of Scope for MVP (explicitly by user decision or marked as TBD):**
  * Detailed elaboration of a complete "Out of Scope" list (User preference: "cannot be said at this time").
  * Support for Right-to-Left (RTL) languages (User preference: "Not planned").
  * Specific training needs and formal training plans for developers or support teams (User preference: "We'll clarify after the MVP").
  * Detailed support and maintenance process for the EAF (User preference: "We'll clarify after the MVP").
  * Fixed release frequency for the EAF (User preference: "We'll clarify after the MVP").
  * Detailed VM specifications for EAF components (User preference: "We'll clarify after the MVP").
  * Backup/Recovery & DR strategies for EAF's own services (User preference: "mark as TBD").
  * Data migration from the legacy "DCA" system (User preference: "not for the MVP").
  * Comprehensive UI for role and permission definition in the RBAC module of the Control Plane (MVP focuses on assigning existing roles).
  * "Password forgotten" flow for the Control Plane UI (MVP focuses on a manual admin process).
* **Future Considerations (Potential future enhancements):**
  * Full-fledged ABAC implementation.
  * Advanced features of the Online License Activation Server.
  * Dynamic loading/unloading of plugins at runtime (beyond ServiceLoader, e.g., with OSGi or similar technologies).
  * Support for additional databases for EAF operation.
  * Integrated feedback mechanisms and UI analytics for the Control Plane.
  * Automated self-service functions for tenant admins in the Control Plane that go beyond the MVP.

## Technical Assumptions

1. **Target Platform:** The ACCI EAF and all applications based on it must be developed and optimized for the **IBM Power Architecture (ppc64le)**.
2. **Operating Environment (MVP Focus):**
    * The **primary target deployment** for the ACCI EAF and applications based on it within the MVP scope is on **Virtual Machines (VMs)**.
    * The EAF and applications based on it **must be designed and runnable in environments without direct internet access (air-gapped environments)**. This also includes aspects like offline licensing and deployment.
    * The use of **public cloud platforms (like AWS, Azure, GCP) or Kubernetes-based environments is not in focus for the MVP and the initial core architecture of the EAF**. The EAF should be designed to operate without dependencies on specific cloud services or Kubernetes as a runtime environment.
    * Although not the primary focus, the EAF's design should, wherever practical and without compromising the primary goals (VMs, offline capability), not create unnecessary hurdles for *eventual future* adaptation or operation of *individual products based on it* in cloud or Kubernetes environments.
3. **Core Technology Stack:** Kotlin (JVM), Spring Boot, Axon Framework, PostgreSQL, Gradle.
4. **Architecture Style:** Modular Monolith.
5. **Key Architectural Patterns:** Hexagonal Architecture, Domain-Driven Design (DDD), Command Query Responsibility Segregation (CQRS), Event Sourcing (ES).
6. **Repository Structure:** Monorepo.
7. **Frontend Technology (for Control Plane UI):** React (oriented towards React-Admin style).
8. **Integration Points (MVP):** External Auth Providers (LDAP, OIDC, SAML), Online License Activation Server, SMTP server for email dispatch.

## Success Metrics & KPIs (MVP)

(Targets within the first 6 months after MVP deployment)

**1. EAF Adoption and Utilization:**

* **KPI 1a:** At least **1-2 significant internal pilot projects/modules** actively use the ACCI EAF.
* **KPI 1b:** Pilot projects utilize at least **60-70%** of the relevant, available EAF core modules.
**2. Developer Productivity and Satisfaction:**
* **KPI 2a:** Onboarding time for new developers (with Kotlin/Java experience) for a standard task using EAF: **< 2-3 working days**.
* **KPI 2b:** Qualitative survey of core EAF users shows >75% agreement on improvement over DCA and increased productivity.
* **KPI 2c:** Developers report a significant (>40-50% estimated) reduction in boilerplate code.
**3. Product Quality & Modernization:**
* **KPI 3a:** Pilot projects successfully implement at least one modern EAF core feature that was difficult/impossible with DCA.
* **KPI 3b:** No critical bugs reported in used EAF core modules within the first 3 months of pilot usage.
**Timeframe for achieving these initial KPIs:** 6 months after MVP deployment.

## Risks and Mitigation (Product Risks)

* **Risk 1: Low adoption of the EAF by internal product teams.**
  * *Mitigation:* Close collaboration with pilot projects, excellent documentation and DX (Epic 10), early demonstration of benefits.
* **Risk 2: High complexity of the EAF overwhelms developers.**
  * *Mitigation:* Strong modularization, clear APIs, comprehensive examples and tutorials (Epic 10), gradual introduction of concepts.
* **Risk 3: Dependency on a few key individuals with deep EAF/Axon knowledge.**
  * *Mitigation:* Knowledge sharing within the team, pair programming, comprehensive documentation, training initiatives (Post-MVP).
* **Risk 4: Performance on ppc64le does not meet expectations.**
  * *Mitigation:* Early benchmarks (NFR 1a), continuous performance monitoring, focus on ppc64le optimizations in the chosen stack.
* **Risk 5: Underestimation of the effort for developing and maintaining the EAF itself.**
  * *Mitigation:* Realistic planning, prioritization of MVP scope, iterative approach, dedicated EAF team.

## Dependencies

* Availability of an SMTP server for email dispatch.
* Availability of test systems for external authentication providers (LDAP, OIDC, SAML) during the development of Epic 8.
* Internal resources (development team Michael, Majlinda, Lirika; QA/Doc Anita; PM Christian) for development and piloting.
* Provisioning and maintenance of the ppc64le VM infrastructure for development, testing, and operation of EAF components.

## Stakeholders

* **Christian:** Product Manager (Primary Contact)
* **Michael:** Fullstack Developer, Staff Engineer (Core EAF Development Team)
* **Majlinda:** Fullstack Developer, Senior Engineer (Core EAF Development Team)
* **Lirika:** Frontend Developer, Junior Engineer (Focus on Control Plane UI)
* **Sebastian:** Backend Developer, Principal Engineer (User/Customer of EAF for one of the first products)
* **Anita:** QA and Documentation (Ensuring quality and documentation of EAF)
* **ACCI Management/Leadership:** Sponsors, strategic decision-makers
* **Administrators of Customer Companies:** Future users of the Control Plane for tenant-specific administration
* **ACCI License Managers:** Internal users of the Control Plane for generating and managing licenses

## Glossary

* **ACCI EAF:** (Axians Competence Center Infrastructure Enterprise Application Framework) The software framework defined herein, developed by the Axians Competence Center Infrastructure Team.
* **API:** Application Programming Interface.
* **Axon Framework:** A Java framework for implementing CQRS, Event Sourcing, and DDD.
* **Build-Logic (Gradle):** A special module in Gradle projects for centralizing build script logic and conventions.
* **CI/CD:** Continuous Integration / Continuous Deployment or Delivery.
* **Control Plane:** A central management and control layer (API and UI) for the ACCI EAF, e.g., for tenant and license management.
* **CQRS:** Command Query Responsibility Segregation.
* **DCA:** (Das Alte [Framework] - The Old [Framework]) The existing, to-be-replaced internal framework at ACCI.
* **DDD:** Domain-Driven Design.
* **Definition of Done (DoD):** A set of criteria that must be met for a User Story to be considered complete. (The detailed DoD was defined previously).
* **EAF:** Enterprise Application Framework.
* **ES:** Event Sourcing.
* **FIPS:** Federal Information Processing Standards.
* **GDPR:** General Data Protection Regulation (German: DSGVO).
* **Gradle:** A build automation tool primarily used in the Java and Kotlin ecosystem.
* **IAM:** Identity & Access Management.
* **i18n:** Internationalization.
* **ISO 27001:** An international standard for information security management systems.
* **JWT:** JSON Web Token.
* **KPI:** Key Performance Indicator.
* **Kotlin:** A modern, statically typed programming language that runs on the JVM.
* **l10n:** Localization.
* **LDAP:** Lightweight Directory Access Protocol.
* **Logback:** A logging framework for Java applications, successor to Log4j.
* **Micrometer:** A vendor-neutral application metrics facade for the JVM ecosystem.
* **Monorepo:** A software development strategy where source code for many different projects is stored in a single repository.
* **MTTR:** Mean Time To Recovery.
* **MVP:** Minimum Viable Product.
* **OAuth 2.0:** Open Authorization.
* **OIDC:** OpenID Connect.
* **OWASP:** Open Web Application Security Project.
* **PicoCLI:** A Java framework for creating command-line applications.
* **Plugin System:** An architecture that allows the functionality of a software to be extended by modules (plugins).
* **PostgreSQL:** An object-relational database management system.
* **ppc64le:** PowerPC 64-bit Little Endian.
* **PRD:** Product Requirements Document.
* **Prometheus:** An open-source system for monitoring and alerting.
* **RBAC:** Role-Based Access Control.
* **React-Admin:** A frontend framework for building admin applications on top of REST/GraphQL APIs, based on React.
* **RLS:** Row-Level Security.
* **SAML:** Security Assertion Markup Language.
* **SBOM:** Software Bill of Materials.
* **ServiceLoader (Java):** A mechanism in Java for dynamically discovering and loading service provider implementations.
* **SLF4J:** Simple Logging Facade for Java.
* **SOC2:** System and Organization Controls 2.
* **SPI:** Service Provider Interface.
* **Spring Boot:** A framework for simplifying the development of Spring-based Java applications.
* **STRIDE:** A threat modeling methodology (Spoofing, Tampering, Repudiation, Information Disclosure, Denial of Service, Elevation of Privilege).
* **UI:** User Interface.
* **UX:** User Experience.
* **VM:** Virtual Machine.

---
**(END PM Tasks START Initial Architect Prompt)**

## Initial Architect Prompt

**To:** Solution Architect
**From:** Christian, Product Manager
**Date:** May 16, 2025
**Subject:** Architecture Design for ACCI Enterprise Application Framework (ACCI EAF) - MVP

Dear Solution Architect,

This document serves as the foundation for the architectural design of the Minimum Viable Product (MVP) for the new **ACCI Enterprise Application Framework (ACCI EAF)**. The goal of this framework is to significantly accelerate, standardize, and improve the quality of the development and maintenance of our enterprise software products for external customers, particularly as a replacement for our outdated "DCA" framework.

### Project Goals (Summary)

* Accelerate the development and standardization of enterprise software products.
* Reduce time and costs in product development.
* Improve the maintainability, security, and performance of end products.
* Provide modern features such as multitenancy, flexible IAM, and license management.

### Core Requirements (Summary)

The ACCI EAF is to be developed as a modular monolith and provide the following core functionalities (MVP) via dedicated modules:

* Comprehensive Multitenancy (RLS, context management, admin API).
* Identity & Access Management (IAM) (local users, service accounts, RBAC, configuration of external providers like LDAP/AD, OIDC, SAML2).
* License Management (time-based, hardware-bound for ppc64le CPU cores, offline/online activation).
* Internationalization (i18n) (loading translations, formatting, language switching, tenant-specific customizations).
* A Plugin System (based on Java ServiceLoader).
* Observability (structured logging, metrics via Micrometer/Prometheus, health checks).
* A web-based Control Plane (Admin UI, React-based) for tenant, user, license, and i18n management.

Detailed functional requirements are documented in this PRD in the form of 10 Epics with User Stories and Acceptance Criteria.

Key Non-Functional Requirements include:

* **Target Platform & Operation:** Exclusively ppc64le VMs, no Cloud/Kubernetes for MVP, offline/air-gapped capability.
* **Security:** High security (OWASP Top 10 A01-A09, Threat Modeling), support for GDPR, ISO27001, SOC2, FIPS. SBOM generation and review.
* **Performance:** Defined response time and throughput targets for core functions and APIs on ppc64le. Load and stress tests are planned.
* **Reliability:** MTTR < 1 hour for critical EAF components (Control Plane, License Server).
* **Maintainability & DX:** High code quality (per "ACCI Kotlin Coding Standards v1.0"), comprehensive documentation, test coverage (100% for core logic).
* **Auditability:** Dedicated audit log for critical operations.

### Technical Assumptions and Mandates

* **Programming Language/Platform:** Kotlin (on the JVM)
* **Core Framework:** Spring Boot
* **Architecture Framework (DDD/CQRS/ES):** Axon Framework
* **Database:** PostgreSQL (for relational data and as Event Store)
* **Build Tool:** Gradle
* **Repository Structure:** Monorepo
* **Architecture Style:** Modular Monolith
* **Key Architectural Patterns:** Hexagonal Architecture, Domain-Driven Design (DDD), Command Query Responsibility Segregation (CQRS), Event Sourcing (ES).
* **Frontend (Control Plane):** React (oriented towards React-Admin style)

### Main Tasks for the Architect

1. Design a robust, maintainable, and extensible software architecture for the ACCI EAF and its core modules (see module structure in the "Core Technical Decisions & Application Structure" section) that meets all functional and non-functional requirements of the MVP.
2. Define clear interfaces between the EAF modules and to the Control Plane API.
3. Detail the database design for PostgreSQL, including schemas for relational data (tenants, users, licenses, etc.) and configuration of the Event Store for Axon.
4. Ensure the architecture optimally supports the implementation of RLS for tenant data isolation.
5. Conceptualize the security architecture (authentication, authorization, protection of APIs and data).
6. Plan the integration points (external auth providers, SMTP, Online License Activation Server).
7. Create necessary architecture diagrams (components, sequences, deployment, etc.).
8. Provide technical guidance to the development team during implementation.

Please use this PRD as the primary source for your architectural decisions. For any questions, I (Christian, PM) am available.

Sincerely,
Christian (Product Manager)

----- END Architect Prompt -----

---
**(END Checklist START Design Architect UI/UX Specification Mode Prompt)**

## Prompt for Design Architect (UI/UX Specification Mode)

**Objective:** Elaborate on the UI/UX aspects of the product defined in this PRD, specifically for the **ACCI EAF Control Plane**.
**Mode:** UI/UX Specification Mode
**Input:** This completed PRD document, particularly the sections "User Interaction and Design Goals," "Functional Requirements" (Epics 2, 3, 4, 6, 8 related to Control Plane features), and "Technical Assumptions" (React-Admin style preference).
**Key Tasks:**

1. Review the product goals, user personas (ACCI Admin, Customer/Tenant Admin), user stories, and UI-related notes herein.
2. Collaboratively define detailed user flows for the key Control Plane administrative tasks (e.g., tenant onboarding, user management within a tenant, license generation by ACCI admin, tenant admin customizing i18n).
3. Create wireframes (conceptual, low-fidelity) or mockups/descriptions for the core screens of the Control Plane UI identified in this PRD.
4. Specify usability requirements and any further interaction details necessary for a "professional and functional" UI in the style of React-Admin.
5. Document these UI/UX specifications, e.g., by creating or populating a `front-end-spec-tmpl.txt` document or by supplementing this PRD with a dedicated UI/UX appendix.
6. Ensure that this PRD and the UI/UX specifications provide a comprehensive foundation for the frontend development of the Control Plane.

Please guide the user (Christian, PM) through this process to enrich the PRD with detailed UI/UX specifications for the Control Plane.

---

This is the English version of the PRD content.

Next, I will provide the completed PM Checklist document as an artifact, and then my final recommendations for the next steps.
