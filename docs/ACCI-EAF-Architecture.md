# ACCI EAF (Axians Competence Center Infrastructure Enterprise Application Framework) Architecture Document

## 1\. Introduction / Preamble

This document outlines the overall project architecture for the ACCI EAF (Axians Competence Center Infrastructure Enterprise Application Framework). This includes backend systems, shared services, and non-UI specific concerns. Its primary goal is to serve as the guiding architectural blueprint for AI-driven development and human developers, ensuring consistency and adherence to chosen patterns and technologies. The ACCI EAF is designed to accelerate the development and standardize enterprise software products provided to external customers, particularly on the IBM Power Architecture (ppc64le).

**Relationship to Frontend Architecture:**
The ACCI EAF includes a "Control Plane UI" for administrative tasks. While this document defines the backend APIs for this UI (within the `eaf-controlplane-api` module) and makes a high-level technology choice for the frontend (React, inspired by React-Admin as per PRD), the detailed frontend architecture (component structure, state management, specific libraries beyond React) would typically be detailed in a separate Frontend Architecture Document. For the scope of this document, we will focus on the backend and core EAF architecture. Core technology stack choices documented herein (see "Definitive Tech Stack Selections") are definitive for the entire project, including any frontend components developed as part of the EAF.

## 2\. Table of Contents

1. Introduction / Preamble
2. Table of Contents
3. Technical Summary
4. High-Level Overview
5. Component View
      * Architectural / Design Patterns Adopted
6. Project Structure
      * Key Directory Descriptions
      * Notes
7. API Reference
      * External APIs Consumed
          * 1. LDAP / Active Directory
          * 2. SMTP Server
          * 3. OpenID Connect (OIDC) Provider
          * 4. SAML 2.0 Identity Provider (IdP)
      * Internal APIs Provided
          * 1. ACCI EAF Control Plane API (`eaf-controlplane-api`)
          * 2. ACCI EAF License Server API (`eaf-license-server`)
8. Data Models
      * Core Application Entities / Domain Objects
          * 1. Tenant
          * 2. User (IAM User)
          * 3. ServiceAccount (IAM Service Account)
          * 4. ActivatedLicense
      * API Payload Schemas (If distinct)
      * Database Schemas (If applicable)
          * 1. Event Store Schema
          * 2. Read Model Schemas (Examples)
          * 3. Configuration / State Data Schemas (Examples)
9. Core Workflow / Sequence Diagrams
      * 1. User Authentication via External OIDC Provider
      * 2. Command Processing and Event Sourcing Flow
      * 3. Tenant Creation in Detail
      * 4. Online License Activation
10. Definitive Tech Stack Selections
11. Infrastructure and Deployment Overview
12. Error Handling Strategy
13. Coding Standards (ACCI Kotlin Coding Standards v1.0)
      * Primary Language & Runtime(s)
      * Style Guide & Linter
      * Naming Conventions
      * File Structure
      * Asynchronous Operations
      * Type Safety
      * Comments & Documentation
      * Dependency Management
      * Detailed Language & Framework Conventions
          * Kotlin Specifics
          * Spring Boot Specifics
          * Axon Framework Specifics
          * Key Library Usage Conventions (General Kotlin/Java)
          * Code Generation Anti-Patterns to Avoid
14. Overall Testing Strategy
15. Security Best Practices
16. Key Reference Documents
17. Change Log

*(The table of contents has been updated to reflect all created sections.)*

## 3\. Technical Summary

The ACCI EAF (Axians Competence Center Infrastructure Enterprise Application Framework) is designed as a **modular monolith** intended for internal use to accelerate the development and standardize enterprise software products deployed to external customers, specifically targeting the IBM Power Architecture (ppc64le). It aims to replace the legacy "DCA" framework, offering significant improvements in features, performance, and developer experience.

The architecture leverages the **Kotlin** programming language on the **JVM**, with **Spring Boot** as the core application framework and **Axon Framework** to implement **Domain-Driven Design (DDD)**, **Command Query Responsibility Segregation (CQRS)**, and **Event Sourcing (ES)** patterns. **PostgreSQL** serves as the primary database for read models, application state, and as the event store. The entire project is managed within a **Monorepo** structure using **Gradle** as the build tool.

Key architectural goals include robust multitenancy, comprehensive identity and access management (IAM), flexible licensing, internationalization, a plugin system for extensibility, and high standards for observability and security. The EAF is structured into core modules like `eaf-core`, `eaf-iam`, `eaf-multitenancy`, `eaf-licensing`, `eaf-observability`, `eaf-internationalization`, and `eaf-plugin-system`, complemented by optional modules such as a CLI for developers and a control plane with a React-based UI for administration.

## 4\. High-Level Overview

The ACCI EAF is designed as a **Modular Monolith**. This architectural style was chosen to balance strong cohesion of core framework functionalities with clear separation of concerns between different EAF modules (e.g., IAM, Licensing, Multitenancy). It also simplifies development and deployment in the initial phases and for the target on-premise VM environments, while still allowing for well-defined internal structure and future scalability of individual modules if necessary. The EAF provides a foundational platform upon which various enterprise applications will be built.

The entire codebase, including all core EAF modules, optional modules, and build logic, is managed in a **Monorepo** using Git. This approach facilitates centralized dependency management (via Gradle Version Catalogs), atomic commits across multiple modules, easier refactoring, and consistent build and testing processes across the entire framework.

Conceptually, the ACCI EAF interacts with its environment and users in a few key ways:

1. **EAF-based Applications:** Software products developed using the ACCI EAF will leverage its core modules (`eaf-core` for CQRS/ES patterns, `eaf-iam` for security, `eaf-multitenancy` for tenant separation, `eaf-licensing` for feature entitlement, etc.) as libraries or foundational components. These applications will implement their specific business logic while relying on the EAF for cross-cutting concerns and standardized enterprise functionalities.
2. **Control Plane UI & API:** Administrators interact with the `eaf-controlplane-api` (typically via the React-based Control Plane UI) to manage tenants, users within tenants, licenses, and internationalization settings for EAF-based applications. This API acts as the administrative interface to the EAF's management capabilities.
3. **Developers:** Developers interact with the EAF by using its defined APIs, extension points (e.g., the plugin system), and potentially CLI tools (`eaf-cli`) to scaffold, build, and maintain applications. The `app-example-module` serves as a practical guide.

The primary data flow for EAF-based applications will often follow CQRS/ES patterns facilitated by `eaf-core` and Axon Framework: commands modify state by creating events that are persisted in the event store (PostgreSQL), and queries read from dedicated read models (also in PostgreSQL) that are updated asynchronously from these events. Administrative operations via the Control Plane API will also interact with the EAF's core modules to manage their respective configurations and data, often also leveraging CQRS/ES principles for auditable changes.

```mermaid
graph TD
    AdminUser[Administrator] -->|Manages via UI| CP_UI[Control Plane UI (React)]
    CP_UI -->|Interacts with API| EAF_CP_API[eaf-controlplane-api]

    Developer[Developer] -->|Uses/Extends| ACCI_EAF[ACCI EAF Modules]
    ACCI_EAF -->|Builds on| JVM[Kotlin/JVM]
    JVM -->|Uses| SpringBoot[Spring Boot]
    SpringBoot -->|Integrates| Axon[Axon Framework]
    Axon -->|Persists to/Reads from| PostgreSQL[PostgreSQL (Event Store / Read Models)]

    subgraph ACCI EAF Modules
        direction LR
        EAF_Core[eaf-core]
        EAF_IAM[eaf-iam]
        EAF_MultiTenancy[eaf-multitenancy]
        EAF_Licensing[eaf-licensing]
        EAF_PluginSystem[eaf-plugin-system]
        EAF_Observability[eaf-observability]
        EAF_i18n[eaf-internationalization]
        EAF_CP_API
    end

    EAF_App[EAF-based Application] -->|Leverages| ACCI_EAF
    EAF_App -->|Serves| EndUser[End User]

    classDef AdminUser fill:#c9f,stroke:#333,stroke-width:2px
    classDef Developer fill:#c9f,stroke:#333,stroke-width:2px
    classDef EndUser fill:#c9f,stroke:#333,stroke-width:2px
    classDef CP_UI fill:#9cf,stroke:#333,stroke-width:2px
    classDef EAF_App fill:#9cf,stroke:#333,stroke-width:2px
```

*{Note: The Mermaid diagram above is an initial conceptual representation. It may be refined and supplemented or replaced by more specific diagrams (e.g., C4 Layer 1 & 2) in the further course.}*

## 5\. Component View

The ACCI EAF is structured as a modular monolith, with distinct logical components (Gradle sub-projects within the monorepo) that encapsulate specific framework functionalities. These components are designed to be cohesive and as loosely coupled as possible, promoting maintainability and independent development where feasible.

Below are the major logical components of the ACCI EAF and their responsibilities:

* **`build-logic`**:
  * *Responsibility:* Contains the central build logic, dependency versions (managed via Gradle Version Catalogs), and build/development conventions for all modules within the monorepo. It ensures consistency in the build process across the EAF.
* **Framework Core Modules:**
  * **`eaf-core`**:
    * *Responsibility:* Provides the fundamental building blocks and core abstractions for EAF-based applications. This includes base classes/interfaces for aggregates, commands, events, common utilities, and the foundational configuration for CQRS (Command Query Responsibility Segregation) and ES (Event Sourcing) patterns, leveraging the Axon Framework. It forms the very heart of applications built using the EAF.
  * **`eaf-iam` (Identity & Access Management)**:
    * *Responsibility:* Implements comprehensive functionalities for user management, authentication (supporting local credential management and integration with external providers like LDAP/AD, OIDC, SAML2), and authorization (Role-Based Access Control - RBAC, with foundational elements for future Attribute-Based Access Control - ABAC). It also includes support for service accounts for system-to-system integrations. This module is designed as a reusable framework component.
  * **`eaf-multitenancy`**:
    * *Responsibility:* Provides the logic and mechanisms to support multitenancy in EAF-based applications. This includes strategies for tenant isolation (e.g., using Row-Level Security in PostgreSQL for data segregation) and managing the tenant context throughout application requests and business logic execution.
  * **`eaf-licensing`**:
    * *Responsibility:* Offers features for license management applicable to applications built with the ACCI EAF. This includes capabilities for defining various license types (e.g., time-limited, hardware-bound) and mechanisms for both offline and online license activation and validation.
  * **`eaf-observability`**:
    * *Responsibility:* Delivers standardized configurations, integrations, and tools for application observability. This encompasses structured logging, metrics exposure compatible with Prometheus (via Micrometer), standardized health check endpoints (leveraging Spring Boot Actuator), and dedicated audit logging capabilities for critical operations.
  * **`eaf-internationalization` (i18n)**:
    * *Responsibility:* Supplies tools, conventions, and a base infrastructure for the internationalization and localization of EAF-based applications. This includes support for managing language resources and tenant-specific translations.
  * **`eaf-plugin-system`**:
    * *Responsibility:* Implements the plugin infrastructure, based on the Java ServiceLoader API. This system allows the EAF itself and applications built upon it to be extended modularly through well-defined service provider interfaces, enabling feature additions without modifying core code.
* **Optional / Supporting Modules:**
  * **`eaf-cli`**:
    * *Responsibility:* Facilitates the development of Command Line Interface (CLI) tools aimed at developers using the EAF. These tools can assist with tasks like project scaffolding, code generation for EAF patterns, and diagnostic utilities.
  * **`app-example-module`**:
    * *Responsibility:* Serves as a reference implementation and a quick-start guide for developers. It demonstrates how a typical business application or a specific domain module can be developed using ACCI EAF components, adhering to its architectural principles and best practices.
  * **`eaf-controlplane-api`**: (Backend for the Control Plane UI)
    * *Responsibility:* Exposes the RESTful APIs required by the Control Plane UI. These APIs allow administrators to manage tenants, users, licenses, and internationalization settings across EAF-based applications.
  * **`eaf-license-server`**: (An EAF-based application itself)
    * *Responsibility:* Provides the server-side logic for online license activation and validation, acting as a central service for products that require this functionality.

**Collaboration and Module Dependencies:**
These modules collaborate to provide a comprehensive application framework. For instance, an incoming request to an EAF-based application would first have its tenant context established by `eaf-multitenancy`. Security checks (authentication and authorization) would be handled by `eaf-iam`. Business logic, potentially using CQRS/ES patterns from `eaf-core`, would then execute. All operations would be subject to observability measures from `eaf-observability`. The `eaf-plugin-system` allows for custom extensions to these flows or the addition of new business capabilities. Administrative functions are exposed via `eaf-controlplane-api`, which in turn uses the other EAF modules to effect changes.

The following diagram illustrates the key modules of the ACCI EAF and their primary dependencies. It distinguishes between "Core EAF Modules" and "Optional/Supporting EAF Applications & Tools", and also shows how a typical "EAF-based Application" would leverage these modules.

```mermaid
graph TD
    subgraph "Core EAF Modules"
        direction LR
        Core["eaf-core"]
        IAM["eaf-iam"]
        MultiTenancy["eaf-multitenancy"]
        Licensing["eaf-licensing"]
        Observability["eaf-observability"]
        I18N["eaf-internationalization"]
        PluginSystem["eaf-plugin-system"]
    end

    subgraph "Optional/Supporting EAF Applications & Tools"
        direction LR
        CLI["eaf-cli"]
        ExampleApp["app-example-module"]
        CP_API["eaf-controlplane-api"]
        LicenseServer["eaf-license-server"]
    end

    %% Core Dependencies: Most core modules depend on eaf-core
    IAM --> Core
    MultiTenancy --> Core
    Licensing --> Core
    Observability --> Core
    I18N --> Core
    PluginSystem --> Core

    %% Application/Tool Dependencies on Core Modules
    CP_API --> Core
    CP_API --> IAM
    CP_API --> MultiTenancy
    CP_API --> Licensing
    CP_API --> I18N
    CP_API --> Observability

    LicenseServer --> Core
    LicenseServer --> Licensing
    LicenseServer --> IAM
    LicenseServer --> Observability
    LicenseServer --> MultiTenancy %% Assuming the license server itself might need to be tenant-aware or secure its own admin access

    ExampleApp --> Core
    ExampleApp --> IAM
    ExampleApp --> MultiTenancy
    ExampleApp --> Licensing
    ExampleApp --> Observability
    ExampleApp --> I18N
    ExampleApp --> PluginSystem %% To demonstrate using/providing plugins

    CLI --> Core %% For shared utilities or understanding EAF project structures

    %% EAF Based applications would use these modules
    ExternalApp["EAF-based Application (General Example)"]
    ExternalApp --> Core
    ExternalApp -.-> IAM
    ExternalApp -.-> MultiTenancy
    ExternalApp -.-> Licensing
    ExternalApp -.-> Observability
    ExternalApp -.-> I18N
    ExternalApp -.-> PluginSystem

    classDef coreModule fill:#D6EAF8,stroke:#2874A6,stroke-width:2px;
    classDef suppModule fill:#D1F2EB,stroke:#117A65,stroke-width:2px;
    classDef externalApp fill:#FEF9E7,stroke:#B7950B,stroke-width:2px;

    class Core,IAM,MultiTenancy,Licensing,Observability,I18N,PluginSystem coreModule;
    class CLI,ExampleApp,CP_API,LicenseServer suppModule;
    class ExternalApp externalApp
```

### 5.1 Architectural / Design Patterns Adopted

The ACCI EAF explicitly adopts several key architectural and design patterns to meet its goals of modularity, maintainability, scalability, and alignment with modern enterprise application development practices. These foundational patterns guide the design of components, their interactions, and technology choices:

* **Modular Monolith:**
  * *Rationale:* Chosen to provide a single deployable unit for on-premise VM environments, simplifying initial operational complexity while enabling strong logical separation and cohesion through well-defined modules (Gradle sub-projects). It allows for focused development within a single codebase and consistent tooling.
* **Hexagonal Architecture (Ports and Adapters):**
  * *Rationale:* To decouple the core application logic (domain and application services) from external concerns such as UI, databases, messaging systems, or other third-party integrations. This is achieved by defining clear "ports" (interfaces in the application core) and "adapters" (implementations of these interfaces for specific technologies). This pattern enhances testability, maintainability, and the ability to swap technologies or integrate with new systems with minimal impact on the core logic. Each EAF module and applications built upon it should strive to follow this pattern internally.
* **Domain-Driven Design (DDD):**
  * *Rationale:* To tackle the complexity of enterprise applications by focusing on the core domain and domain logic. DDD principles like ubiquitous language, aggregates, entities, value objects, repositories, and domain services will be applied to model the business problems accurately within the EAF modules (e.g., `eaf-iam`, `eaf-licensing`) and to guide developers building applications on the EAF.
* **Command Query Responsibility Segregation (CQRS):**
  * *Rationale:* To separate the model used for updating information (commands) from the model used for reading information (queries). This allows for optimization of each side independently. For example, the command side can focus on consistency and validation (leveraging DDD aggregates), while the query side can use denormalized read models optimized for specific query needs, enhancing performance and scalability. Axon Framework provides strong support for implementing this pattern.
* **Event Sourcing (ES):**
  * *Rationale:* To capture all changes to an application state as a sequence of immutable events. Instead of storing only the current state, the EAF will store the full history of what happened. This provides strong audit capabilities, facilitates debugging and temporal queries, and enables the rebuilding of state or the creation of new read model projections from the event log. ES is a natural fit with CQRS and DDD, and is also a core feature supported by Axon Framework using PostgreSQL as the event store.

## 6\. Project Structure

The ACCI EAF is developed as a Monorepo managed with Git. Gradle is used as the build system, orchestrating the build of all modules (Gradle sub-projects). The following diagram and descriptions outline the proposed project folder structure. This structure is designed to support the modular monolith architecture and separate concerns effectively.

```plaintext
ACCI-EAF/
├── .github/                    # CI/CD workflows (e.g., GitHub Actions)
│   └── workflows/
│       └── ci.yml
├── .vscode/                    # VSCode settings (optional)
│   └── settings.json
├── build-logic/                # Central Gradle build logic (convention plugins)
│   └── src/main/kotlin/        # Custom tasks, plugins for build consistency
├── docs/                       # Project documentation
│   ├── ACCI-EAF-PRD.md
│   ├── ACCI-EAF-Architecture.md
│   └── adr/                    # Architectural Decision Records
├── gradle/                     # Gradle wrapper files
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── eaf-core/                   # Core EAF module (foundational APIs, CQRS/ES base)
│   ├── src/main/kotlin/        # Kotlin source code
│   ├── src/main/resources/     # Resource files
│   ├── src/test/kotlin/        # Unit and integration tests for this module
│   └── build.gradle.kts        # Module-specific Gradle build script
├── eaf-iam/                    # Identity & Access Management module
│   └── ... (similar structure to eaf-core)
├── eaf-multitenancy/           # Multitenancy module
│   └── ...
├── eaf-licensing/              # Licensing module
│   └── ...
├── eaf-observability/          # Observability module
│   └── ...
├── eaf-internationalization/   # Internationalization (i18n) module
│   └── ...
├── eaf-plugin-system/          # Plugin system infrastructure module
│   └── ...
├── eaf-cli/                    # Optional: CLI tools for EAF developers
│   └── ...
├── app-example-module/         # Optional: Example application/module using the EAF
│   └── ...
├── eaf-controlplane-api/       # Optional: Backend API for the Control Plane (Spring Boot application)
│   ├── src/main/kotlin/        # Kotlin source for the API application
│   ├── src/main/resources/
│   ├── src/test/kotlin/
│   └── build.gradle.kts
├── eaf-license-server/         # Optional: License Server application (built on EAF)
│   ├── src/main/kotlin/        # Kotlin source for the License Server application
│   ├── src/main/resources/
│   ├── src/test/kotlin/
│   └── build.gradle.kts
├── controlplane-ui/            # Optional: Frontend React application for the Control Plane
│   ├── public/
│   ├── src/                    # React component sources, etc.
│   ├── package.json
│   ├── tsconfig.json
│   └── build.gradle.kts        # Optional: Gradle build for frontend (e.g., using Node plugin for integration)
├── test/                       # Top-level tests (e.g., E2E tests spanning multiple modules) - (Optional)
│   └── e2e/
├── .env.example                # Example environment variables
├── .gitattributes              # Git attributes for line endings, etc.
├── .gitignore                  # Specifies intentionally untracked files that Git should ignore
├── build.gradle.kts            # Root Gradle build file (common configurations, plugin versions)
├── settings.gradle.kts         # Gradle settings file (declares all modules/sub-projects)
├── gradlew                     # Gradle wrapper script for Unix-like systems
├── gradlew.bat                 # Gradle wrapper script for Windows
└── README.md                   # Project overview, setup, and usage instructions
```

### 6.1 Key Directory Descriptions

* **`ACCI-EAF/`**: The root directory of the monorepo.
* **`.github/workflows/`**: Contains CI/CD pipeline definitions (e.g., for GitHub Actions), including build, test, and potentially release workflows.
* **`build-logic/`**: Houses custom Gradle convention plugins and shared build configurations to ensure consistency across all modules (e.g., Kotlin versions, common dependencies, compiler options). This is a standard Gradle approach for managing complex builds.
* **`docs/`**: Contains all project-related documentation, including this Architecture Document, the PRD, and potentially ADRs (Architectural Decision Records) in the `docs/adr/` subdirectory, diagrams, etc.
* **`gradle/wrapper/`**: Contains the Gradle Wrapper, allowing developers to build the project with a consistent Gradle version without needing a system-wide installation.
* **`eaf-*` (e.g., `eaf-core/`, `eaf-iam/`)**: These directories represent the individual modules (Gradle sub-projects) of the ACCI EAF. Each module typically follows a standard Kotlin/Gradle project structure:
  * **`src/main/kotlin/`**: Contains the main Kotlin source code for the module.
    * Within this, Hexagonal Architecture principles should be applied by organizing code into packages like `domain` (core business logic, entities, value objects, domain events), `application` (use cases, application services orchestrating domain logic), and `adapters` (or `infrastructure`) for implementations of ports (e.g., REST controllers, database repositories, event listeners).
  * **`src/main/resources/`**: Contains resource files for the module (e.g., Spring Boot `application.yml` specific to a module if it's a runnable app, database migration scripts, i18n bundles).
  * **`src/test/kotlin/`**: Contains unit and integration tests for the module's code. Test file organization should mirror the `src/main/kotlin/` structure.
  * **`build.gradle.kts`**: The Gradle build script specific to this module, declaring its dependencies, plugins, and build configurations.
* **`eaf-controlplane-api/`**: A specific Spring Boot application module providing the backend for the administrative UI.
* **`eaf-license-server/`**: Another specific Spring Boot application module, built using EAF principles, to handle online license operations.
* **`controlplane-ui/`**: Contains the source code for the React-based frontend of the Control Plane. This is a standard Node.js project structure (e.g., created with Create React App or similar). Its detailed internal structure would be defined in a separate frontend architecture document if one were created. It might have its own `build.gradle.kts` if its build is integrated into the main Gradle build.
* **`test/e2e/`**: (Optional) For end-to-end tests that validate workflows across multiple modules or the entire system.
* **`build.gradle.kts` (root)**: The main Gradle build file at the root of the project. It typically defines common configurations, plugin versions (using the plugin management block), and applies plugins to sub-projects.
* **`settings.gradle.kts`**: The Gradle settings file. It is crucial as it declares all the sub-projects (modules like `eaf-core`, `eaf-iam`, etc.) that are part of the multi-project build.

### 6.2 Notes

* **Build Output:** Compiled artifacts (JARs, WARs if any) for each module will typically be found in their respective `build/libs/` directories. Runnable applications (like `eaf-controlplane-api` or `eaf-license-server`) will produce executable JARs.
* **IDE Integration:** This structure is standard for Gradle multi-project builds and should be well-supported by IDEs like IntelliJ IDEA (with excellent Kotlin and Gradle support).
* **Hexagonal Structure within Modules:** As mentioned in "Key Directory Descriptions", it's expected that individual EAF modules (especially those containing significant business logic like `eaf-iam` or `eaf-licensing`) will internally adopt a package structure reflecting Hexagonal Architecture (e.g., `com.axians.accieaf.[module].domain`, `com.axians.accieaf.[module].application`, `com.axians.accieaf.[module].adapter.rest`, `com.axians.accieaf.[module].adapter.persistence`). This will be further detailed in the "Coding Standards" section.

## 7\. API Reference

This section details the Application Programming Interfaces (APIs) that the ACCI EAF system will interact with, both those consumed from external sources and those provided by its own components.

### 7.1 External APIs Consumed

The ACCI EAF, particularly through its `eaf-iam` and notification capabilities, will consume the following external services/protocols:

#### 7.1.1 LDAP / Active Directory

* **Purpose:** Used by `eaf-iam` for external user authentication and to fetch user attributes (e.g., group memberships, email addresses) from an enterprise directory service.
* **Protocol:** Lightweight Directory Access Protocol (LDAP v3)
* **Connection Parameters (to be configured per deployment/tenant):**
  * LDAP Server Hostname(s): `{ldap_host}`
  * LDAP Server Port: `{ldap_port}` (e.g., 389 for LDAP, 636 for LDAPS)
  * Use SSL/TLS (LDAPS): `true/false`
  * Use StartTLS: `true/false` (if port is 389 and SSL/TLS is desired)
  * Bind DN (Service Account for searches, optional): `{bind_dn_user}`
  * Bind Password (Service Account Password, optional): `{bind_dn_password}` (Stored securely, e.g., via environment variables or mounted secrets)
  * User Search Base DN: `{user_search_base}` (e.g., `ou=users,dc=example,dc=com`)
  * User Search Filter: `{user_search_filter}` (e.g., `(&(objectClass=person)(sAMAccountName={0}))` or `(&(objectClass=inetOrgPerson)(uid={0}))`)
  * Group Search Base DN: `{group_search_base}` (e.g., `ou=groups,dc=example,dc=com`)
  * Group Search Filter: `{group_search_filter}` (e.g., `(&(objectClass=group)(member={0}))`)
  * Attribute Mappings: (Configurable map of EAF user attributes to LDAP attributes, e.g., `username -> sAMAccountName`, `email -> mail`, `displayName -> displayName`, `groups -> memberOf`)
* **Authentication Method for EAF Service Account:** Simple BIND with the configured Bind DN and password (if anonymous binding is not sufficient for searches).
* **Authentication Method for End Users:** Simple BIND operation against the LDAP server using the username (transformed via search filter) and password provided by the user.
* **Key Operations Used by `eaf-iam`:**
  * **BIND Operation:**
    * Description: To authenticate a user by attempting to bind to the LDAP server with their provided credentials.
    * Also used by the EAF service account (if configured) to establish a connection for searching.
  * **SEARCH Operation:**
    * Description: To find a user's DN based on their login name, and to fetch user attributes (e.g., for creating a user profile in the EAF or checking group memberships for authorization).
    * To find groups a user is a member of.
* **Data Format:** LDAP Data Interchange Format (LDIF) for entries and attributes.
* **Error Handling:** LDAP result codes (e.g., "Invalid Credentials", "No Such Object", "Server Down") will be caught and mapped to appropriate EAF internal exceptions or error responses. Connection timeouts and search timeouts must be configured.
* **Link to Official Docs:**
  * LDAP v3: [RFC 4510-4519](https://datatracker.ietf.org/doc/html/rfc4510) (and related RFCs)
  * Active Directory: Microsoft documentation for AD LDAP.

#### 7.1.2 SMTP Server

* **Purpose:** Used by various EAF modules or EAF-based applications for sending email notifications (e.g., password reset links, system alerts, license notifications).
* **Protocol:** Simple Mail Transfer Protocol (SMTP)
* **Connection Parameters (to be configured per deployment):**
  * SMTP Server Hostname: `{smtp_host}`
  * SMTP Server Port: `{smtp_port}` (e.g., 25, 465 for SMTPS, 587 for SMTP with STARTTLS)
  * Authentication Required: `true/false`
  * Username (if auth required): `{smtp_username}`
  * Password (if auth required): `{smtp_password}` (Stored securely)
  * Transport Security: None / SSL/TLS (SMTPS) / STARTTLS
  * Default Sender Address ("From"): `{default_from_address}`
* **Authentication Method:** Typically SMTP AUTH (e.g., PLAIN, LOGIN, CRAM-MD5) if required by the server.
* **Key Operations Used:**
  * **Sending an Email:**
    * Description: The EAF will construct an email message (headers, body) and transmit it to the configured SMTP server for delivery.
    * Key SMTP commands involved: `EHLO/HELO`, `MAIL FROM`, `RCPT TO`, `DATA`.
* **Data Format:** Email messages formatted according to RFC 5322 (Internet Message Format) and related MIME standards (RFC 2045-2049).
* **Error Handling:** SMTP reply codes (e.g., "550 No such user here", "421 Service not available") will be handled. Connection errors, timeouts, and authentication failures will be logged and may trigger retry mechanisms or alert administrators.
* **Link to Official Docs:**
  * SMTP: [RFC 5321](https://datatracker.ietf.org/doc/html/rfc5321)
  * MIME: [RFC 2045-2049](https://datatracker.ietf.org/doc/html/rfc2045)

#### 7.1.3 OpenID Connect (OIDC) Provider

* **Purpose:** Used by `eaf-iam` to enable external user authentication via OpenID Connect 1.0, allowing users to log in with their existing accounts from a configured OIDC Identity Provider (IdP).
* **Protocol:** OpenID Connect 1.0 (built on OAuth 2.0).
* **Interaction Type:** The EAF acts as an OIDC Relying Party (RP). The typical flow will be the Authorization Code Flow.
* **Key Endpoints & Metadata (provided by the OIDC Provider):**
  * **Discovery Endpoint (`/.well-known/openid-configuration`):**
    * Description: A well-known URI where the OIDC Provider publishes its metadata. This metadata includes URLs for other necessary endpoints (Authorization, Token, UserInfo, JWKS), supported scopes, response types, claims, and cryptographic algorithms.
    * `eaf-iam` will fetch and use this metadata to configure its interaction with the OIDC provider dynamically or at setup.
  * **Authorization Endpoint:**
    * Description: The endpoint at the OIDC Provider where the user is redirected by the EAF to authenticate and grant consent.
    * Interaction: EAF redirects the user's browser to this URL with parameters like `client_id`, `response_type=code`, `scope`, `redirect_uri`, `state`, `nonce`.
  * **Token Endpoint:**
    * Description: The endpoint at the OIDC Provider where the EAF (RP) exchanges an authorization code (received via redirect from Authorization Endpoint) for an ID Token, Access Token, and optionally a Refresh Token.
    * Interaction: EAF makes a direct (server-to-server) POST request to this URL with parameters like `grant_type=authorization_code`, `code`, `redirect_uri`, `client_id`, `client_secret`.
  * **UserInfo Endpoint (Optional):**
    * Description: An OAuth 2.0 protected resource at the OIDC Provider where the EAF can retrieve additional claims about the authenticated user using the Access Token obtained from the Token Endpoint.
    * Interaction: EAF makes a GET or POST request to this URL, including the Access Token in the `Authorization` header.
  * **JWKS (JSON Web Key Set) URI:**
    * Description: An endpoint where the OIDC Provider publishes its public keys (in JWK format) used to sign ID Tokens.
    * Interaction: `eaf-iam` will fetch these keys to validate the signature of received ID Tokens.
* **Authentication (EAF RP to OIDC Provider):**
  * The EAF is registered as a client with the OIDC Provider and receives a `client_id`.
  * For the Token Endpoint, the EAF authenticates using its `client_id` and a `client_secret` (or other client authentication methods like private key JWT).
* **Request/Response Data Formats:**
  * **ID Token:** A JSON Web Token (JWT) containing claims about the authentication event and the user (e.g., `iss` (issuer), `sub` (subject/user ID), `aud` (audience/client ID), `exp` (expiration), `iat` (issued at), `nonce`, `email`, `name`, `preferred_username`). The ID Token is the primary artifact for authentication.
  * **Access Token:** Usually an opaque string for the EAF, used to authorize access to the UserInfo Endpoint. Format is specific to the OIDC Provider.
  * **UserInfo Response:** JSON object containing user claims.
* **Key Configuration Parameters (per OIDC Provider / Tenant):**
  * Issuer URL (e.g., `https://idp.example.com/oidc`)
  * Client ID (obtained from OIDC Provider registration)
  * Client Secret (obtained from OIDC Provider registration, stored securely)
  * Redirection URI(s) (EAF's endpoint(s) where users are redirected back after authentication, e.g., `https://eaf-app.example.com/login/oauth2/code/{registrationId}`)
  * Requested Scopes (e.g., `openid`, `profile`, `email`, custom scopes)
  * Attribute Mappings (if needed, to map OIDC claims to EAF user profile attributes)
  * Preferred JWS (JSON Web Signature) algorithm for ID Token validation.
* **Error Handling:** OAuth 2.0 and OIDC specific error codes (e.g., `invalid_request`, `unauthorized_client`, `access_denied`, `invalid_grant`) returned by the OIDC Provider will be handled. ID Token validation failures (signature, issuer, audience, expiry, nonce) must lead to authentication failure.
* **Link to Official Docs:**
  * OpenID Connect Core 1.0: [https://openid.net/specs/openid-connect-core-1\_0.html](https://openid.net/specs/openid-connect-core-1_0.html)
  * OpenID Connect Discovery 1.0: [https://openid.net/specs/openid-connect-discovery-1\_0.html](https://openid.net/specs/openid-connect-discovery-1_0.html)

#### 7.1.4 SAML 2.0 Identity Provider (IdP)

* **Purpose:** Used by `eaf-iam` to enable external user authentication via SAML 2.0 Web Browser SSO Profile, allowing users to log in using their existing enterprise identities.
* **Protocol:** SAML (Security Assertion Markup Language) 2.0.
* **Interaction Type:** The EAF acts as a SAML Service Provider (SP). The typical flow will be the Web Browser SSO Profile (often SP-initiated).
* **Key Endpoints & Metadata:**
  * **IdP Metadata:**
    * Description: An XML document provided by the SAML IdP that describes its services, endpoints (e.g., SingleSignOnService, SingleLogoutService), supported bindings (e.g., HTTP-Redirect, HTTP-POST), and X.509 certificates used for signing and encryption.
    * Interaction: `eaf-iam` (SP) consumes this metadata to configure trust and interaction parameters with the IdP. This can be provided via a URL or by uploading the metadata file.
  * **SP Metadata:**
    * Description: An XML document generated or configured by `eaf-iam` (SP) that describes its own services, ACS URL, SLO URL, entity ID, and X.509 certificates.
    * Interaction: This metadata is provided to the SAML IdP to configure the trust relationship from the IdP's side.
  * **SingleSignOnService (SSO) Endpoint (on IdP):**
    * Description: The IdP's endpoint where the EAF (SP) sends SAML AuthnRequests (Authentication Requests) or redirects the user's browser for authentication.
    * Bindings: Typically HTTP-Redirect or HTTP-POST.
  * **Assertion Consumer Service (ACS) Endpoint (on EAF/SP):**
    * Description: The EAF's endpoint that receives SAML Assertions (containing authentication statements and attributes) from the IdP via the user's browser (typically via HTTP-POST).
  * **SingleLogoutService (SLO) Endpoint (on IdP and SP, Optional):**
    * Description: Endpoints used to facilitate single logout, allowing a user to log out from all federated sessions.
* **Authentication & Trust:**
  * Trust is established by exchanging SAML metadata between the EAF (SP) and the IdP.
  * Messages (AuthnRequests from SP, Assertions from IdP) are typically digitally signed using XML Signature with X.509 certificates whose public keys are exchanged via metadata. Assertions may also be encrypted using XML Encryption.
* **Request/Response Data Formats:**
  * **SAML AuthnRequest:** An XML document sent by the EAF (SP) to the IdP to request user authentication.
  * **SAML Assertion:** An XML document issued by the IdP upon successful user authentication. It contains statements about the authentication event, the authenticated subject (user), attributes, and conditions under which the assertion is valid.
* **Key Configuration Parameters (per SAML IdP / Tenant):**
  * IdP Metadata URL or XML content.
  * SP Entity ID (EAF's unique identifier for this SAML integration, e.g., `https://eaf-app.example.com/saml/metadata`).
  * SP ACS URL (e.g., `https://eaf-app.example.com/login/saml2/sso/{registrationId}`).
  * SP private key and certificate for signing AuthnRequests and decrypting Assertions (if encrypted).
  * NameID Policy (format of the user identifier expected from the IdP).
  * Attribute Mappings (to map SAML Assertion attributes to EAF user profile attributes).
  * Binding types to use for requests and responses (e.g., HTTP-POST, HTTP-Redirect).
* **Error Handling:** SAML status codes within responses indicate success or failure. Validation failures of SAML Assertions (signature, issuer, audience, conditions, subject confirmation, replay attacks) must lead to authentication failure. Errors during protocol exchange (e.g., invalid AuthnRequest) are also possible.
* **Link to Official Docs:**
  * OASIS SAML 2.0 Standard: [https://www.oasis-open.org/standards\#samlv2.0](https://www.google.com/search?q=https://www.oasis-open.org/standards%23samlv2.0) (includes Core, Bindings, Profiles specifications).

### 7.2 Internal APIs Provided

#### 7.2.1 ACCI EAF Control Plane API (`eaf-controlplane-api`)

* **Purpose:** This API provides RESTful endpoints for administrators to manage core aspects of the ACCI EAF ecosystem. It serves as the backend for the React-based Control Plane UI, enabling operations related to tenant management, user administration within tenants, license Cconfiguration, internationalization settings, and identity provider configurations.

* **Base URL(s):**

  * Proposed: `/controlplane/api/v1` (The actual deployment URL will depend on the environment.)

* **Authentication/Authorization:**

  * **Authentication:** All endpoints are protected. Clients (i.e., the Control Plane UI used by administrators) must authenticate. This will be handled by `eaf-iam`, likely using dedicated administrative user accounts with credentials (e.g., username/password). The authentication mechanism will be token-based (e.g., JWTs issued upon successful login).
  * **Authorization:** Granular permissions will be enforced based on administrative roles (e.g., SuperAdmin, TenantAdmin) managed within `eaf-iam`. For example, some operations might only be available to SuperAdmins, while others might be delegated to TenantAdmins for their specific tenant.

* **General API Conventions:**

  * Data Format: JSON for request and response bodies.
  * Error Handling: Uses standard HTTP status codes (e.g., `400 Bad Request`, `401 Unauthorized`, `403 Forbidden`, `404 Not Found`, `500 Internal Server Error`). Error responses will include a JSON body with details (e.g., `errorCode`, `message`, `details`).
  * Idempotency: `PUT` and `DELETE` operations should be idempotent. `POST` operations for creation may not be.
  * Pagination: List endpoints will use query parameters for pagination (e.g., `page`, `size`).
  * Sorting & Filtering: List endpoints may support sorting (e.g., `sort=fieldName,asc`) and filtering based on specific field values.

* **Key Endpoints (Illustrative examples, to be fully specified by OpenAPI definitions):**

  * **Tenant Management:**

    * **`POST /tenants`**: Create a new tenant.
      * Request Body Schema: `{ "name": "string", "description"?: "string", "status": "ACTIVE"|"INACTIVE", ... }`
      * Success Response Schema (201 Created): `{ "id": "string", "name": "string", ... }` (Full tenant details)
    * **`GET /tenants`**: List all tenants (paginated, filterable).
      * Success Response Schema (200 OK): `[{ "id": "string", "name": "string", "status": "string", ... }]`
    * **`GET /tenants/{tenantId}`**: Get details of a specific tenant.
      * Success Response Schema (200 OK): `{ "id": "string", "name": "string", ... }`
    * **`PUT /tenants/{tenantId}`**: Update a tenant.
      * Request Body Schema: `{ "name"?: "string", "description"?: "string", "status"?: "ACTIVE"|"INACTIVE", ... }`
      * Success Response Schema (200 OK): `{ "id": "string", "name": "string", ... }`
    * **`DELETE /tenants/{tenantId}`**: Deactivate/delete a tenant (logical or hard delete TBD).
      * Success Response Schema (204 No Content or 200 OK with status)

  * **User Management (within a Tenant):**

    * **`POST /tenants/{tenantId}/users`**: Create a new local user within a tenant.
      * Request Body Schema: `{ "username": "string", "email"?: "string", "firstName"?: "string", "lastName"?: "string", "initialPassword"?: "string", "roles": ["string"], ... }`
      * Success Response Schema (201 Created): `{ "id": "string", "username": "string", "email"?: "string", ... }`
    * **`GET /tenants/{tenantId}/users`**: List users in a tenant.
      * Success Response Schema (200 OK): `[{ "id": "string", "username": "string", "email"?: "string", ... }]`
    * **`GET /tenants/{tenantId}/users/{userId}`**: Get details of a specific user.
    * **`PUT /tenants/{tenantId}/users/{userId}`**: Update user details (e.g., status, roles, profile info).
    * **`POST /tenants/{tenantId}/users/{userId}/reset-password`**: Initiate a password reset for a user.

  * **Service Account Management (within a Tenant):**

    * **`POST /tenants/{tenantId}/service-accounts`**: Create a service account for a tenant.
      * Request Body Schema: `{ "name": "string", "description"?: "string", "roles": ["string"] }`
      * Success Response Schema (201 Created): `{ "id": "string", "name": "string", "clientId": "string", "clientSecret"?: "string (returned on creation only)", ... }`
    * **`GET /tenants/{tenantId}/service-accounts`**: List service accounts for a tenant.
    * **`DELETE /tenants/{tenantId}/service-accounts/{accountId}`**: Delete a service account.

  * **Identity Provider Configuration (per Tenant):**

    * **`POST /tenants/{tenantId}/identity-providers`**: Configure an external IdP (LDAP, OIDC, SAML) for a tenant.
      * Request Body Schema: `{ "type": "LDAP"|"OIDC"|"SAML", "name": "string", "configuration": "object (schema varies by type)", "enabled": "boolean" }` (e.g., for LDAP: host, port, baseDN; for OIDC: issuerUrl, clientId, clientSecret)
      * Success Response Schema (201 Created): `{ "id": "string", "type": "string", "name": "string", ... }`
    * **`GET /tenants/{tenantId}/identity-providers`**: List configured IdPs for a tenant.
    * **`PUT /tenants/{tenantId}/identity-providers/{idpId}`**: Update an IdP configuration.

  * **License Management (Potentially global or assignable to tenants):**

    * **`POST /licenses`**: Create a new license definition (for ACCI Team).
      * Request Body Schema: `{ "productName": "string", "type": "TIME_LIMITED"|"FEATURE_BASED"|"HARDWARE_BOUND", "validityPeriodDays"?: "number", "features": ["string"], "maxActivations"?: "number", ... }`
    * **`GET /licenses`**: List all license definitions.
    * **`POST /tenants/{tenantId}/assigned-licenses`**: Assign/link a license to a tenant or activate a license for a tenant.
      * Request Body Schema: `{ "licenseId": "string", "activationDetails"?: "object" }`

  * **Internationalization (i18n) Management (per Tenant):**

    * **`GET /tenants/{tenantId}/i18n/languages`**: List supported/configured languages for a tenant.
    * **`PUT /tenants/{tenantId}/i18n/languages`**: Set supported languages for a tenant.
    * **`GET /tenants/{tenantId}/i18n/translations/{langCode}`**: Get all translations for a specific language for a tenant.
      * Success Response Schema (200 OK): `{ "key1": "translation1", "key2": "translation2", ... }`
    * **`PUT /tenants/{tenantId}/i18n/translations/{langCode}`**: Update/set translations for a language.
      * Request Body Schema: `{ "key1": "new_translation1", ... }`

* **Rate Limits:** To be defined, but appropriate rate limiting should be implemented to protect the API from abuse.

* **Link to Detailed API Specification:** *(Placeholder: An OpenAPI (Swagger) specification will be generated and maintained for this API as part of the development process. It will reside in `docs/api/controlplane-v1.yml` or be available via a Swagger UI endpoint.)*

#### 7.2.2 ACCI EAF License Server API (`eaf-license-server`)

* **Purpose:** This API provides centralized online services for activating, validating, and potentially deactivating licenses for applications built using the ACCI EAF. It enables scenarios where a deployed application instance needs to confirm its license status with a remote server. Licenses are generally issued at a "customer" level.

* **Base URL(s):**

  * Proposed: `/licenseserver/api/v1` (The actual deployment URL will depend on the environment.)

* **Authentication/Authorization:**

  * **Authentication:** All endpoints are strictly protected. Client applications (EAF-based applications requiring license checks) must authenticate to this server.
    * **Method 1 (Preferred):** Using client credentials (e.g., a unique `clientId` and `clientSecret` or a signed JWT) issued per EAF-based application, effectively identifying the "customer" or a specific deployment context. These credentials could be managed via the `eaf-controlplane-api` and securely distributed to the client applications. This interaction would leverage `eaf-iam` concepts.
    * **Method 2 (Alternative):** A pre-shared secret or API key specific to the deployed application instance, combined with other identifying information (e.g., product code, instance ID).
  * **Authorization:** Operations might be authorized based on the authenticated client application's identity (representing the customer) and the specific license key or activation ID being referenced. The server will verify if the requesting application/customer is entitled to perform the operation on the given license.

* **General API Conventions:**

  * Data Format: JSON for request and response bodies.
  * Error Handling: Uses standard HTTP status codes. Error responses will include a JSON body with details (e.g., `errorCode`, `message`, `validationErrors`).
  * Idempotency: Key operations should be designed with idempotency in mind where appropriate (e.g., re-validating an already active license should return its current status without side effects).

* **Key Endpoints (Illustrative examples, to be fully specified by OpenAPI definitions):**

  * **License Activation:**

    * **`POST /activations`**: Attempts to activate a license for a product instance (customer level).
      * Request Body Schema:

                ```json
                {
                  "productCode": "string",
                  "licenseKey": "string",
                  "hardwareIds": ["string"],
                  "instanceId": "string"
                }
                ```

      * Success Response Schema (200 OK or 201 Created):

                ```json
                {
                  "activationId": "string",
                  "status": "ACTIVE",
                  "productName": "string",
                  "licenseType": "TIME_LIMITED" | "FEATURE_BASED" | "PERPETUAL",
                  "expiresAt": "iso-datetime | null",
                  "activatedAt": "iso-datetime",
                  "features": ["string"],
                  "validationIntervalSeconds": "number | null"
                }
                ```

      * Error Response Schema (e.g., 400, 403, 404):

                ```json
                {
                  "errorCode": "string",
                  "message": "string"
                }
                ```

  * **License Validation:**

    * **`POST /validations`**: Validates the current status of an activated license. This is called periodically by the client application.
      * Request Body Schema:

                ```json
                {
                  "activationId": "string",
                  "productCode": "string",
                  "hardwareIds": ["string"],
                  "instanceId": "string"
                }
                ```

      * Success Response Schema (200 OK):

                ```json
                {
                  "activationId": "string",
                  "status": "ACTIVE" | "EXPIRED" | "INVALID" | "REVOKED",
                  "productName": "string",
                  "licenseType": "string",
                  "expiresAt": "iso-datetime | null",
                  "features": ["string"],
                  "validationIntervalSeconds": "number | null",
                  "message": "string | null"
                }
                ```

      * Error Response Schema (e.g., 400, 403, 404):

                ```json
                {
                  "errorCode": "string",
                  "message": "string"
                }
                ```

  * **License Deactivation (Optional):**

    * **`DELETE /activations/{activationId}`**: Deactivates a previously activated license for a specific product instance.
      * Request Parameters: `activationId` (path parameter).
      * Request Body Schema (Optional, may require additional proof/context):

                ```json
                {
                  "productCode": "string",
                  "instanceId": "string",
                  "reason": "string | null"
                }
                ```

      * Success Response Schema (200 OK or 204 No Content):

                ```json
                {
                  "status": "DEACTIVATED",
                  "deactivatedAt": "iso-datetime"
                }
                ```

      * Error Response Schema (e.g., 400, 403, 404):

                ```json
                {
                  "errorCode": "string",
                  "message": "string"
                }
                ```

* **Rate Limits:** To be defined. Calls to `/validations` might be frequent from many deployed instances, so appropriate strategies (e.g., per `activationId` or per client IP/application ID representing the customer) are necessary.

* **Link to Detailed API Specification:** *(Placeholder: An OpenAPI (Swagger) specification will be generated and maintained for this API as part of the development process. It will reside in `docs/api/licenseserver-v1.yml` or be available via a Swagger UI endpoint.)*

## 8\. Data Models

This section defines the main data structures used within the ACCI EAF. This includes core domain objects, considerations for API payloads, and database schema structures for both the event store and read models. Given the use of Kotlin, data structures are exemplified using Kotlin data classes or interfaces.

### 8.1 Core Application Entities / Domain Objects

These are the central concepts that the ACCI EAF and applications built upon it will manage. In an Event Sourcing context (using Axon Framework), many of
these will be represented as Aggregates, whose state is derived from a sequence of events. The definitions below represent the typical state of these aggregates or key entities.

#### 8.1.1 Tenant

* **Description:** Represents a customer or a distinct organizational unit using EAF-based applications. Tenants provide a scope for user management, licensing, and other configurations. Managed by `eaf-multitenancy` and `eaf-controlplane-api`.
* **Schema / Data Class Definition (Kotlin):**

    ```kotlin
    data class Tenant(
        val id: String, // Aggregate identifier (UUID)
        val name: String, // Name of the tenant
        val description: String? = null,
        val status: TenantStatus = TenantStatus.ACTIVE,
        val createdAt: java.time.Instant,
        val updatedAt: java.time.Instant,
        // Potentially other configuration details specific to a tenant
        val identityProviderConfigurations: List<IdentityProviderConfigSummary> = emptyList(),
        val assignedLicenseInfo: AssignedLicenseSummary? = null
    )

    enum class TenantStatus {
        ACTIVE,
        INACTIVE,
        SUSPENDED
    }

    data class IdentityProviderConfigSummary(
        val idpId: String,
        val name: String,
        val type: String // e.g., "OIDC", "SAML", "LDAP"
    )

    data class AssignedLicenseSummary(
        val licenseId: String,
        val productName: String,
        val expiresAt: java.time.Instant?
    )
    ```

* **Validation Rules:** `id` is mandatory and unique. `name` is mandatory. `status` must be one of the defined enum values.

#### 8.1.2 User (IAM User)

* **Description:** Represents an individual end-user or administrator within the context of a Tenant. Users have credentials for authentication and are assigned roles for authorization. Managed by `eaf-iam` and `eaf-controlplane-api`.
* **Schema / Data Class Definition (Kotlin):**

    ```kotlin
    data class User(
        val id: String, // Aggregate identifier (UUID)
        val tenantId: String, // Identifier of the tenant this user belongs to
        val username: String, // Unique username within the tenant
        val email: String? = null, // Optional email address of the user (not necessarily unique)
        var firstName: String? = null,
        var lastName: String? = null,
        var displayName: String? = null,
        val status: UserStatus = UserStatus.ACTIVE,
        var passwordHash: String? = null, // For local users; salted and hashed
        val externalIdpSubject: String? = null, // Subject from external IdP if federated
        val identityProviderAlias: String? = null, // Alias of the IdP used for federation
        val roles: Set<String> = emptySet(), // Set of role identifiers assigned to the user
        val createdAt: java.time.Instant,
        var updatedAt: java.time.Instant,
        var lastLoginAt: java.time.Instant? = null
    )

    enum class UserStatus {
        PENDING_VERIFICATION, // e.g., email verification needed if email is provided
        ACTIVE,
        INACTIVE, // Disabled by admin
        LOCKED // Locked due to failed login attempts, etc.
    }
    ```

* **Validation Rules:** `id`, `tenantId`, `username` are mandatory. `username` must be unique within the tenant. `passwordHash` is required for local users not using an external IdP (unless other primary authentication methods are configured). `roles` should reference valid Role entities.

#### 8.1.3 ServiceAccount (IAM Service Account)

* **Description:** Represents a non-human actor (e.g., an application, a service) that needs to authenticate and authorize with EAF-protected resources or APIs, typically within a specific tenant's context. Managed by `eaf-iam` and `eaf-controlplane-api`.
* **Schema / Data Class Definition (Kotlin):**

    ```kotlin
    data class ServiceAccount(
        val id: String, // Aggregate identifier (UUID)
        val tenantId: String,
        val name: String, // A descriptive name for the service account
        val description: String? = null,
        val clientId: String, // Unique client identifier
        var clientSecretHash: String? = null, // Salted and hashed client secret (only if using client secret auth)
                                            // Alternatively, public keys for JWT client assertion
        val status: ServiceAccountStatus = ServiceAccountStatus.ACTIVE,
        val roles: Set<String> = emptySet(), // Roles assigned to this service account
        val createdAt: java.time.Instant,
        var updatedAt: java.time.Instant,
        var secretExpiresAt: java.time.Instant? = null
    )

    enum class ServiceAccountStatus {
        ACTIVE,
        INACTIVE
    }
    ```

* **Validation Rules:** `id`, `tenantId`, `name`, `clientId` are mandatory and unique. `clientSecretHash` is set upon creation/rotation.

#### 8.1.4 ActivatedLicense

* **Description:** Represents an instance of a license that has been activated for a customer's product deployment. This is the primary entity managed by the `eaf-license-server` and associated with licensing information in `eaf-licensing`.
* **Schema / Data Class Definition (Kotlin):**

    ```kotlin
    data class ActivatedLicense(
        val activationId: String, // Aggregate identifier (UUID), unique for this activation
        val licenseKey: String, // The master license key that was activated
        val customerId: String, // Identifier for the customer this license belongs to (derived from licenseKey or auth context)
        val productCode: String,
        val instanceId: String, // Identifier for the specific product instance
        var status: LicenseActivationStatus = LicenseActivationStatus.PENDING,
        val hardwareIds: List<String> = emptyList(),
        val activatedAt: java.time.Instant,
        var lastValidatedAt: java.time.Instant? = null,
        var expiresAt: java.time.Instant? = null, // If the license is time-limited
        val features: List<String> = emptyList(), // Features enabled by this license activation
        val deactivationReason: String? = null,
        var updatedAt: java.time.Instant
    )

    enum class LicenseActivationStatus {
        PENDING, // Initial state, e.g., awaiting first validation
        ACTIVE,
        EXPIRED,
        REVOKED, // Manually revoked by an administrator
        DEACTIVATED, // Gracefully deactivated by the client application
        INVALID_HARDWARE // Activation is invalid due to hardware mismatch
    }
    ```

* **Validation Rules:** `activationId`, `licenseKey`, `customerId`, `productCode`, `instanceId` are mandatory. `status` reflects the lifecycle.

*{Further core entities such as `Role`, `Permission`, `LicenseDefinition`, `IdentityProviderConfig`, `I18NTranslationBundle`, `AuditEvent`, `PluginDescriptor`, etc., would be defined here in a similar manner.}*

### 8.2 API Payload Schemas (If distinct)

As a general principle, the request and response payload schemas for the HTTP APIs (detailed in the "API Reference" section for `eaf-controlplane-api` and `eaf-license-server`) are directly derived from the "Core Application Entities / Domain Objects" defined above, or are specific subsets/DTOs (Data Transfer Objects) tailored for a particular API operation.

For example, a `POST` request to create a `Tenant` would likely take a payload resembling the `Tenant` data class but without system-generated fields like `id`, `createdAt`, or `updatedAt`. The response would then typically include the fully populated `Tenant` object.

Specific request and response schemas, including precise field names, data types, and validation rules (e.g., mandatory fields, format constraints), have been illustratively outlined in the "API Reference" section. The definitive and most detailed specification for all API payloads will be maintained in the OpenAPI (Swagger) documents generated alongside the development of the respective API modules (e.g., `docs/api/controlplane-v1.yml`, `docs/api/licenseserver-v1.yml`).

Reusable, complex payload structures that are distinct from core entities and used across multiple API endpoints (e.g., standardized error response formats, pagination wrappers) will also be defined within these OpenAPI specifications. For instance, a common error response payload might look like:

```json
{
  "timestamp": "iso-datetime",
  "status": "integer (HTTP status code)",
  "error": "string (HTTP error phrase)",
  "message": "string (developer-friendly error message)",
  "path": "string (request path)",
  "details": [
    {
      "field": "string (field causing the error, if applicable)",
      "issue": "string (description of the issue)"
    }
  ]
}
```

### 8.3 Database Schemas (If applicable)

The ACCI EAF utilizes PostgreSQL as its primary database system. Given the adoption of CQRS and Event Sourcing with Axon Framework, the database serves multiple purposes:

1. **Event Store:** Persisting all domain events generated by the aggregates.
2. **Read Models (Query Models):** Storing denormalized data projections optimized for querying and UI display.
3. **State Data:** Storing configuration data or other stateful information for EAF modules that may not be event-sourced.

#### 8.3.1 Event Store Schema

The ACCI EAF will use Axon Framework's JDBC implementation for its Event Store, with PostgreSQL as the backing database. Axon Framework provides a standard, predefined schema for storing domain events and snapshots. The key tables include:

* **`DOMAINEVENTS` (or `domain_event_entry` in newer Axon versions):** Stores the serialized domain events, including the aggregate identifier, sequence number, event type, payload, and metadata.
* **`SNAPSHOTEVENTS` (or `snapshot_event_entry`):** Stores snapshots of aggregates to optimize loading times for aggregates with long event histories.
* Other Axon-specific tables for tracking tokens for event processors (`token_entry`), saga state (`saga_entry`), etc., may also be part of this schema depending on the Axon features used.

The exact DDL for these tables is provided by Axon Framework and will be applied during the initial setup or via database migration tools (e.g., Flyway, Liquibase) configured for the EAF. For details, refer to the official Axon Framework documentation regarding JDBC Event Storage.

#### 8.3.2 Read Model Schemas (Examples)

Read models are specifically designed relational tables in PostgreSQL that provide optimized query capabilities for the `eaf-controlplane-api`, EAF-based applications, and any other query consumers. These tables are populated by event listeners/processors that subscribe to the domain events from the event store.

Below are some illustrative examples of DDL for read model tables:

* **`read_tenants` Table:** For querying tenant information.

    ```sql
    CREATE TABLE read_tenants (
        tenant_id VARCHAR(36) PRIMARY KEY,
        name VARCHAR(255) NOT NULL,
        description TEXT,
        status VARCHAR(50) NOT NULL, -- e.g., 'ACTIVE', 'INACTIVE'
        created_at TIMESTAMP WITH TIME ZONE NOT NULL,
        updated_at TIMESTAMP WITH TIME ZONE NOT NULL
        -- Additional denormalized fields for querying can be added here
    );
    CREATE INDEX idx_read_tenants_name ON read_tenants(name);
    CREATE INDEX idx_read_tenants_status ON read_tenants(status);
    ```

* **`read_users` Table:** For querying user information.

    ```sql
    CREATE TABLE read_users (
        user_id VARCHAR(36) PRIMARY KEY,
        tenant_id VARCHAR(36) NOT NULL REFERENCES read_tenants(tenant_id),
        username VARCHAR(255) NOT NULL,
        email VARCHAR(255), -- Optional, as per User entity
        first_name VARCHAR(255),
        last_name VARCHAR(255),
        display_name VARCHAR(255),
        status VARCHAR(50) NOT NULL, -- e.g., 'ACTIVE', 'LOCKED'
        is_external_auth BOOLEAN DEFAULT FALSE, -- True if federated via external IdP
        created_at TIMESTAMP WITH TIME ZONE NOT NULL,
        updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
        last_login_at TIMESTAMP WITH TIME ZONE
        -- Roles might be stored in a separate join table (read_user_roles) or as an array/JSON if simple
    );
    CREATE UNIQUE INDEX idx_read_users_tenant_username ON read_users(tenant_id, username);
    CREATE INDEX idx_read_users_email ON read_users(email);
    CREATE INDEX idx_read_users_status ON read_users(status);
    ```

* **`read_activated_licenses` Table:** For querying activated license information.

    ```sql
    CREATE TABLE read_activated_licenses (
        activation_id VARCHAR(36) PRIMARY KEY,
        license_key VARCHAR(255) NOT NULL,
        customer_id VARCHAR(255) NOT NULL, -- Identifier of the customer
        product_code VARCHAR(100) NOT NULL,
        instance_id VARCHAR(255) NOT NULL,
        status VARCHAR(50) NOT NULL, -- e.g., 'ACTIVE', 'EXPIRED', 'REVOKED'
        hardware_ids TEXT, -- Comma-separated or JSON array of hardware IDs
        activated_at TIMESTAMP WITH TIME ZONE NOT NULL,
        last_validated_at TIMESTAMP WITH TIME ZONE,
        expires_at TIMESTAMP WITH TIME ZONE,
        features TEXT, -- Comma-separated or JSON array of enabled features
        updated_at TIMESTAMP WITH TIME ZONE NOT NULL
    );
    CREATE INDEX idx_read_activated_licenses_license_key ON read_activated_licenses(license_key);
    CREATE INDEX idx_read_activated_licenses_customer_id ON read_activated_licenses(customer_id);
    CREATE INDEX idx_read_activated_licenses_product_code ON read_activated_licenses(product_code);
    ```

*(Further read model tables for service accounts, IdP configurations, roles, license definitions, etc., will be defined analogously as needed.)*

#### 8.3.3 Configuration / State Data Schemas (Examples)

Some EAF modules might require storing configuration or state data that is not event-sourced but managed more like traditional relational data.

* **`iam_roles` Table:** For defining roles available in the system (used by `eaf-iam`).

    ```sql
    CREATE TABLE iam_roles (
        role_id VARCHAR(100) PRIMARY KEY, -- e.g., 'SUPER_ADMIN', 'TENANT_ADMIN', 'USER'
        description TEXT,
        is_system_role BOOLEAN DEFAULT FALSE -- Indicates if it's a core EAF role
    );
    -- Permissions associated with roles might be in a separate iam_role_permissions table
    -- or defined in code if static for system roles.
    ```

* **`licensing_definitions` Table:** For storing master license definitions (used by `eaf-licensing`).

    ```sql
    CREATE TABLE licensing_definitions (
        license_def_id VARCHAR(36) PRIMARY KEY,
        product_name VARCHAR(255) NOT NULL,
        license_type VARCHAR(50) NOT NULL, -- e.g., 'TIME_LIMITED', 'PERPETUAL', 'FEATURE_BASED'
        default_duration_days INTEGER, -- If time-limited
        default_features TEXT, -- Comma-separated or JSON array
        max_activations INTEGER,
        notes TEXT,
        created_at TIMESTAMP WITH TIME ZONE NOT NULL,
        updated_at TIMESTAMP WITH TIME ZONE NOT NULL
    );
    ```

Database schema migrations for read models and configuration/state tables will be managed using **Liquibase** (Version `4.31.1`), integrated into the build and deployment process.

## 9\. Core Workflow / Sequence Diagrams

This section illustrates key or complex workflows using Mermaid sequence diagrams. These diagrams help in understanding the interactions between different components of the ACCI EAF and external systems.

### 9.1 User Authentication via External OIDC Provider

This sequence diagram shows the typical "Authorization Code Flow" when a user authenticates to an EAF-based application using an external OpenID Connect (OIDC) Provider. The `eaf-iam` module within the EAF-based application handles the OIDC Relying Party (RP) logic.

```mermaid
sequenceDiagram
    actor UserBrowser as User (Browser)
    participant EAFApp as EAF-based Application
    participant EAFiam as eaf-iam Module
    participant OIDCProvider as External OIDC Provider

    UserBrowser->>+EAFApp: 1. Access Protected Resource
    EAFApp->>EAFiam: 2. Verify authentication
    alt User Not Authenticated
        EAFiam->>EAFApp: 3. Initiate OIDC Authentication
        EAFApp->>EAFiam: 4. Prepare OIDC AuthN Request (construct AuthN URL)
        EAFiam-->>EAFApp: AuthN URL with client_id, redirect_uri, scope, state, nonce
        EAFApp-->>-UserBrowser: 5. Redirect to OIDC Provider (Authorization Endpoint)
    end

    UserBrowser->>+OIDCProvider: 6. Authenticates with OIDC Provider (e.g., enters credentials)
    OIDCProvider-->>-UserBrowser: 7. Redirect back to EAF App (Redirect URI with Authorization Code & state)

    UserBrowser->>+EAFApp: 8. Request to EAF App's Redirect URI (with Authorization Code)
    EAFApp->>EAFiam: 9. Process OIDC callback (pass Authorization Code, state)
    EAFiam->>+OIDCProvider: 10. Exchange Authorization Code for Tokens (Token Endpoint)<br/>(sends code, client_id, client_secret, redirect_uri)
    OIDCProvider-->>-EAFiam: 11. ID Token, Access Token, (Refresh Token)

    EAFiam->>EAFiam: 12. Validate ID Token (signature, issuer, audience, expiry, nonce)
    alt ID Token Valid
        opt Fetch UserInfo
            EAFiam->>+OIDCProvider: 13. Request UserInfo (UserInfo Endpoint with Access Token)
            OIDCProvider-->>-EAFiam: 14. UserInfo Response (claims)
        end
        EAFiam->>EAFApp: 15. Authentication successful, establish session<br/>(user context with claims created/updated)
        EAFApp-->>-UserBrowser: 16. Serve Protected Resource
    else ID Token Invalid
        EAFiam->>EAFApp: Authentication failed
        EAFApp-->>-UserBrowser: Show error page / Redirect to login
    end
```

**Description of the flow:**

1. **Access Protected Resource:** The user attempts to access a protected resource in an application built on ACCI EAF.
2. **Verify Authentication:** The EAF-based Application, utilizing the `eaf-iam` module, checks if the user is already authenticated.
3. **Initiate OIDC Authentication:** If the user is not authenticated, `eaf-iam` determines that OIDC authentication should be initiated (based on tenant or application configuration).
4. **Prepare OIDC AuthN Request:** `eaf-iam` constructs the URL for the OIDC Provider's Authorization Endpoint, including parameters like `client_id`, `redirect_uri`, requested `scope`s (e.g., `openid profile email`), a `state` parameter (for CSRF protection), and a `nonce` (for replay protection).
5. **Redirect to OIDC Provider:** The EAF-based Application redirects the user's browser to the OIDC Provider.
6. **User Authenticates with OIDC Provider:** The user interacts with the OIDC Provider to authenticate (e.g., enters username/password, performs MFA).
7. **Redirect back to EAF App:** Upon successful authentication, the OIDC Provider redirects the user's browser back to the `redirect_uri` registered by the EAF-based Application. This redirect includes an `authorization_code` and the original `state` parameter.
8. **Request to Redirect URI:** The user's browser makes a request to the EAF-based Application's redirect URI, delivering the `authorization_code`.
9. **Process OIDC Callback:** The EAF-based Application passes the `authorization_code` and `state` to `eaf-iam`. `eaf-iam` first validates the `state` parameter.
10. **Exchange Authorization Code for Tokens:** `eaf-iam` makes a direct (server-to-server) request to the OIDC Provider's Token Endpoint, exchanging the `authorization_code` for an ID Token, an Access Token, and optionally a Refresh Token. This request is authenticated using the EAF application's `client_id` and `client_secret`.
11. **ID Token, Access Token Returned:** The OIDC Provider returns the requested tokens.
12. **Validate ID Token:** `eaf-iam` meticulously validates the ID Token:
      * Signature verification using the OIDC Provider's public keys (obtained via JWKS URI).
      * Validation of claims like `iss` (issuer), `aud` (audience, must match `client_id`), `exp` (expiration time), `iat` (issued at time), and `nonce` (must match the one sent in step 4).
13. **(Optional) Request UserInfo:** If needed, and if an Access Token was received, `eaf-iam` can use the Access Token to request additional user claims from the OIDC Provider's UserInfo Endpoint.
14. **UserInfo Response:** The OIDC Provider returns the additional user claims.
15. **Authentication Successful, Establish Session:** If all validations pass, `eaf-iam` considers the user authenticated. It creates a local security context/session for the user within the EAF-based Application. User information (from ID Token and UserInfo endpoint claims) may be used to provision or update a local representation of the user within the `eaf-iam` user store for the tenant.
16. **Serve Protected Resource:** The EAF-based Application now serves the originally requested protected resource to the authenticated user.

### 9.2 Command Processing and Event Sourcing Flow

This sequence diagram illustrates the typical flow of processing a command, generating domain events, persisting these events (Event Sourcing), and updating read models (CQRS) within an application built on the ACCI EAF, utilizing Axon Framework.

```mermaid
sequenceDiagram
    participant Client as Client (e.g., UI, API Consumer)
    participant AppService as EAF App Service/API Endpoint
    participant CmdGateway as Axon Command Gateway
    participant Aggregate as Target Aggregate (e.g., TenantAggregate)
    participant EvtStore as Axon Event Store (PostgreSQL)
    participant EvtBus as Axon Event Bus
    participant Projector as Event Handler / Projector
    participant ReadDB as Read Model Database (PostgreSQL)

    Client->>+AppService: 1. Send Command (e.g., CreateTenantCommand with data)
    AppService->>CmdGateway: 2. Construct & Dispatch Command Object
    CmdGateway->>+Aggregate: 3. Route Command to @CommandHandler method
    Aggregate->>Aggregate: 4. Validate Command (against current aggregate state)
    alt Command Valid
        Aggregate->>Aggregate: 5. Apply Domain Event(s) (e.g., TenantCreatedEvent)<br/>(using AggregateLifecycle.apply())
        Note over Aggregate: Internal @EventSourcingHandler updates aggregate state
        Aggregate-->>CmdGateway: (Command handling successful)
        CmdGateway-->>AppService: 6. (Optional) Command result (e.g., aggregateId)
        AppService-->>-Client: 7. (Optional) HTTP Response (e.g., 201 Created with ID)

        Aggregate->>EvtBus: 8. Event(s) published to Event Bus (by Axon)
        EvtBus->>EvtStore: 9. Persist Event(s) (by Axon)
        EvtStore-->>EvtBus: (Persistence successful)

        EvtBus->>+Projector: 10. Event(s) delivered to @EventHandler method
        Projector->>+ReadDB: 11. Update Read Model(s) (e.g., INSERT into read_tenants)
        ReadDB-->>-Projector: (Update successful)
        Projector-->>-EvtBus: (Event processing complete)
    else Command Invalid
        Aggregate-->>CmdGateway: Exception (e.g., validation failed)
        CmdGateway-->>AppService: Propagate Exception
        AppService-->>-Client: HTTP Error Response (e.g., 400 Bad Request)
    end
```

**Description of the flow:**

1. **Send Command:** A client (e.g., a user interacting with the Control Plane UI, an external system calling an API, or another service within the EAF) initiates an action by sending a command. A command is an intent to change the state of an aggregate (e.g., `CreateTenantCommand`, `UpdateUserEmailCommand`). It typically carries the data necessary for the operation.
2. **Construct & Dispatch Command Object:** The application service or API endpoint in the EAF-based application receives the command data, constructs a formal command object (a DTO representing the command), and dispatches it through Axon's `CommandGateway`.
3. **Route Command to Command Handler:** The `CommandGateway` routes the command object to the appropriate `@CommandHandler` method within the designated DDD Aggregate (e.g., `TenantAggregate`). Axon Framework ensures that the target aggregate instance is loaded from the Event Store (rehydrated from its past events) or newly created if it doesn't exist yet (e.g., for creation commands).
4. **Validate Command:** The `@CommandHandler` method within the aggregate contains the business logic to validate the command against the current state of the aggregate and any business rules.
5. **Apply Domain Event(s):** If the command is valid, the `@CommandHandler` does not directly change the aggregate's state. Instead, it makes a decision and *applies* one or more domain events that represent the outcome of the command (e.g., `TenantCreatedEvent`, `UserEmailUpdatedEvent`). This is typically done using `AggregateLifecycle.apply(eventObject)` in Axon.
      * Internally, when an event is applied, a corresponding `@EventSourcingHandler` method within the same aggregate is invoked with the event. This handler is responsible for updating the aggregate's in-memory state based on the event's content.
6. **(Optional) Command Result:** After the command handler has successfully processed the command (i.e., applied events), it might return a result (e.g., the ID of the newly created aggregate, or void if no direct result is needed). This result is passed back through the `CommandGateway`.
7. **(Optional) HTTP Response:** The application service/API endpoint can then return an appropriate HTTP response to the client (e.g., HTTP 201 Created with the new resource ID, or HTTP 200 OK).
8. **Event(s) Published to Event Bus:** After the command handler method completes successfully and the unit of work is committed, Axon Framework publishes the applied domain event(s) to the `EventBus`.
9. **Persist Event(s):** Axon Framework also ensures that these domain events are durably persisted to the configured Event Store (in this case, the `DOMAINEVENTS` table in PostgreSQL via Axon's JDBC event storage mechanism). This is the "Event Sourcing" part.
10. **Event(s) Delivered to Event Handler:** Other components in the system, known as Event Handlers or Projectors (often annotated with `@EventHandler`), subscribe to specific types of events on the `EventBus`. When relevant events are published, Axon delivers them to these handlers.
11. **Update Read Model(s):** The Event Handler (Projector) processes the event and updates one or more read models (denormalized views of the data stored in separate tables in the Read Model Database, e.g., `read_tenants` in PostgreSQL). These read models are optimized for querying and serving data to UIs or other query clients.

This CQRS/ES flow ensures a clear separation of concerns, provides a full audit trail through the event store, and allows for flexible and scalable read model projections.

### 9.3 Tenant Creation in Detail

This sequence diagram illustrates the process of an administrator creating a new tenant via the Control Plane UI. This involves interactions between the UI, the `eaf-controlplane-api`, the `eaf-multitenancy` module (which would manage a `TenantAggregate` using Axon Framework), and potentially `eaf-iam` for setting up initial tenant-specific configurations or users (though the latter is simplified in this diagram for focus).

```mermaid
sequenceDiagram
    actor Admin as Administrator
    participant CP_UI as Control Plane UI (React)
    participant CP_API as eaf-controlplane-api
    participant CmdGateway as Axon Command Gateway (in CP_API)
    participant TenantAgg as TenantAggregate (e.g., in eaf-multitenancy)
    participant EvtStore as Axon Event Store (PostgreSQL)
    participant EvtBus as Axon Event Bus
    participant TenantProjector as TenantReadModelProjector
    participant ReadDB as Read Model DB (PostgreSQL)

    Admin->>+CP_UI: 1. Fills Tenant Creation Form (name, description, etc.)
    CP_UI->>+CP_API: 2. POST /controlplane/api/v1/tenants (with tenant data)
    CP_API->>CmdGateway: 3. Construct & Dispatch CreateTenantCommand
    CmdGateway->>+TenantAgg: 4. Route Command to @CommandHandler (new Aggregate instance)
    TenantAgg->>TenantAgg: 5. Validate Command (e.g., unique name if required)
    alt Command Valid
        TenantAgg->>TenantAgg: 6. Apply TenantCreatedEvent (with tenantId, name, etc.)
        Note over TenantAgg: @EventSourcingHandler updates aggregate state
        TenantAgg-->>CmdGateway: (Command handling successful, returns tenantId)
        CmdGateway-->>CP_API: 7. tenantId returned
        CP_API-->>-CP_UI: 8. HTTP 201 Created (with tenantId and representation)

        TenantAgg->>EvtBus: 9. TenantCreatedEvent published (by Axon)
        EvtBus->>EvtStore: 10. Persist TenantCreatedEvent (by Axon)
        EvtStore-->>EvtBus: (Persistence successful)

        EvtBus->>+TenantProjector: 11. TenantCreatedEvent delivered
        TenantProjector->>+ReadDB: 12. Insert new tenant record into 'read_tenants' table
        ReadDB-->>-TenantProjector: (Update successful)
        TenantProjector-->>-EvtBus: (Event processing complete)
        
        CP_UI-->>-Admin: 13. Display Success (Tenant Created)
        
        Note right of CP_API: Optionally, CP_API could now issue<br/>a subsequent command to eaf-iam<br/>to create a default admin user for this new tenant.
    else Command Invalid
        TenantAgg-->>CmdGateway: Exception (e.g., validation failed)
        CmdGateway-->>CP_API: Propagate Exception
        CP_API-->>-CP_UI: HTTP Error (e.g., 400 Bad Request with error details)
        CP_UI-->>-Admin: Display Error
    end
```

**Description of the flow:**

1. **Fill Form:** An administrator uses the Control Plane UI to fill in the details for creating a new tenant (e.g., name, description).
2. **Submit Request:** The UI sends a `POST` request with the tenant data to the `eaf-controlplane-api`.
3. **Dispatch Command:** The API controller in `eaf-controlplane-api` receives the request, validates it, constructs a `CreateTenantCommand` object, and dispatches it using Axon's `CommandGateway`.
4. **Route to Aggregate:** The `CommandGateway` routes the command to the `@CommandHandler` method in the `TenantAggregate`. Since this is a new tenant, Axon Framework instantiates a new `TenantAggregate`.
5. **Validate Command:** The `TenantAggregate` validates the command (e.g., ensures the tenant name meets criteria, checks for uniqueness if required by business rules).
6. **Apply Event:** If valid, the `TenantAggregate` applies a `TenantCreatedEvent`, capturing all necessary data for the new tenant. The `@EventSourcingHandler` within the aggregate updates its state based on this event.
7. **Command Result:** The command handler successfully completes, potentially returning the new `tenantId`.
8. **HTTP Response to UI:** The `eaf-controlplane-api` returns a success response (e.g., HTTP 201 Created) to the Control Plane UI, including the new tenant's ID and representation.
9. **Event Published:** Axon Framework publishes the `TenantCreatedEvent` to the `EventBus`.
10. **Event Persisted:** Axon Framework persists the `TenantCreatedEvent` to the Event Store (PostgreSQL).
11. **Event Delivered to Projector:** The `TenantReadModelProjector`, an event handler subscribed to `TenantCreatedEvent`, receives the event.
12. **Update Read Model:** The projector creates a new record for the tenant in the `read_tenants` table (or other relevant read models) in the Read Model Database.
13. **Display Success:** The Control Plane UI informs the administrator that the tenant was successfully created.
    *Note: As indicated in the diagram, after successful tenant creation, the `eaf-controlplane-api` might initiate subsequent commands, for example, to the `eaf-iam` module to provision an initial administrator user for the newly created tenant. This follow-up action is part of the overall business process but separated for clarity in this specific aggregate's command flow.*

### 9.4 Online License Activation

This sequence diagram outlines the process where an EAF-based application instance performs an online activation of its license by communicating with the `eaf-license-server`. The `eaf-license-server` itself is an EAF-based application using CQRS/ES principles to manage `ActivatedLicenseAggregate`s.

```mermaid
sequenceDiagram
    participant EAFApp as EAF-based Application (Client)
    participant LicenseServerAPI as eaf-license-server (REST API)
    participant LSCmdGateway as Axon Command Gateway (in License Server)
    participant ActivatedLicAgg as ActivatedLicenseAggregate (in License Server)
    participant LSEvtStore as Axon Event Store (PostgreSQL, for License Server)
    participant LSEvtBus as Axon Event Bus (in License Server)
    participant LicProjector as LicenseReadModelProjector (in License Server)
    participant LSReadDB as Read Model DB (PostgreSQL, for License Server)

    EAFApp->>+LicenseServerAPI: 1. POST /licenseserver/api/v1/activations<br/>(productCode, licenseKey, hardwareIds, instanceId)
    Note over EAFApp, LicenseServerAPI: Application authenticates to License Server
    LicenseServerAPI->>LSCmdGateway: 2. Construct & Dispatch ActivateLicenseCommand
    LSCmdGateway->>+ActivatedLicAgg: 3. Route Command to @CommandHandler
    ActivatedLicAgg->>ActivatedLicAgg: 4. Validate Command (check licenseKey validity,<br/>activation limits, hardwareIds, etc.)
    alt Command Valid
        ActivatedLicAgg->>ActivatedLicAgg: 5. Apply LicenseActivatedEvent (with activationId, features, expiry, etc.)
        Note over ActivatedLicAgg: @EventSourcingHandler updates aggregate state
        ActivatedLicAgg-->>LSCmdGateway: (Command handling successful, returns activation details)
        LSCmdGateway-->>LicenseServerAPI: 6. Activation details (activationId, status, features)
        LicenseServerAPI-->>-EAFApp: 7. HTTP 200 OK / 201 Created (with activation details)

        ActivatedLicAgg->>LSEvtBus: 8. LicenseActivatedEvent published (by Axon)
        LSEvtBus->>LSEvtStore: 9. Persist LicenseActivatedEvent (by Axon)
        LSEvtStore-->>LSEvtBus: (Persistence successful)

        LSEvtBus->>+LicProjector: 10. LicenseActivatedEvent delivered
        LicProjector->>+LSReadDB: 11. Insert/Update 'read_activated_licenses' record
        LSReadDB-->>-LicProjector: (Update successful)
        LicProjector-->>-LSEvtBus: (Event processing complete)
        
        EAFApp->>EAFApp: 12. Store activation details locally
    else Command Invalid
        ActivatedLicAgg-->>LSCmdGateway: Exception (e.g., license invalid, limit reached)
        LSCmdGateway-->>LicenseServerAPI: Propagate Exception
        LicenseServerAPI-->>-EAFApp: HTTP Error (e.g., 400 Bad Request with error code)
    end
```

**Description of the flow:**

1. **Request Activation:** An EAF-based application, upon initialization or when required, sends a `POST` request to the `/activations` endpoint of the `eaf-license-server`. The request includes the `productCode`, the customer's `licenseKey`, current `hardwareIds` (if applicable for node-locking), and a unique `instanceId` for the application instance. The application authenticates itself to the `eaf-license-server`.
2. **Dispatch Command:** The API controller in `eaf-license-server` receives the request, validates it, constructs an `ActivateLicenseCommand`, and dispatches it via its internal Axon `CommandGateway`.
3. **Route to Aggregate:** The `CommandGateway` routes the command to the `@CommandHandler` in the `ActivatedLicenseAggregate`. Axon loads or creates an aggregate instance, potentially identified by the `licenseKey` or a composite key.
4. **Validate Command:** The `ActivatedLicenseAggregate` performs validation:
      * Checks the validity and entitlements of the provided `licenseKey` (this might involve looking up a `LicenseDefinition` from its own read models or a shared configuration).
      * Verifies if the license allows further activations (e.g., checks against `maxActivations`).
      * Compares `hardwareIds` with any existing activations for the license, if hardware-binding is enforced.
5. **Apply Event:** If validation passes, the aggregate applies a `LicenseActivatedEvent`. This event contains all relevant details like a unique `activationId`, the features enabled by this license, expiration date, etc. The `@EventSourcingHandler` within the aggregate updates its state.
6. **Command Result:** The command handler returns the successful activation details.
7. **HTTP Response to Application:** The `eaf-license-server` API sends a success response (e.g., HTTP 200 OK or 201 Created) back to the EAF-based application, including the `activationId`, current `status`, list of enabled `features`, and `expiresAt` date.
8. **Event Published:** Axon Framework publishes the `LicenseActivatedEvent` to the internal `EventBus` of the `eaf-license-server`.
9. **Event Persisted:** Axon Framework persists the `LicenseActivatedEvent` to its Event Store (PostgreSQL).
10. **Event Delivered to Projector:** The `LicenseReadModelProjector` (an event handler within `eaf-license-server`) receives the event.
11. **Update Read Model:** The projector creates or updates the record for this activation in the `read_activated_licenses` table in its Read Model Database.
12. **Store Activation Locally:** The EAF-based application receives the successful activation details and should store them locally (e.g., in a configuration file, secure storage) for future offline validations and to avoid repeated online activations.

## 10\. Definitive Tech Stack Selections

This section outlines the definitive technology choices for the ACCI EAF project. These selections are the single source of truth for all technology decisions. Other architecture documents (e.g., for the Frontend) must refer to these choices and elaborate on their specific application rather than re-defining them.

**Notes on Versioning:**

* **Exact Versions:** Specific, exact versions (e.g., `1.2.3`) are required. Ranges are not permitted.
* **"Latest Stable":** If "Latest Stable" is indicated, it refers to the latest stable version available as of the "Last Document Update" date. The actual version number used (e.g., `library@2.3.4`) must be recorded here. Pinning versions is strongly preferred.
* **Last Document Update:** 16. Mai 2025

**Preferred Starter Templates:**

* Preferred Starter Template Frontend (Control Plane UI): React-Admin. For other potential frontend applications, Vite-based starter templates are preferred.
* Preferred Starter Template Backend: Spring Initializr ([start.spring.io](https://start.spring.io/)) for Spring Boot module structure. For Axon Framework specific structure, official examples and recommended project layouts will be followed.

| Category             | Technology              | Version / Details                      | Description / Purpose                                       | Justification (Optional)                                                                 |
| :------------------- | :---------------------- | :------------------------------------- | :---------------------------------------------------------- | :--------------------------------------------------------------------------------------- |
| **Languages** | Kotlin                  | 2.1.21                                 | Primary language for backend EAF modules and applications   | Modern, concise, null-safe, excellent Java interoperability, good IDE support. Specified by PRD. |
| **Runtime** | JVM (IBM Semeru Runtimes for Power or OpenJDK) | Java 21 (LTS)                          | Execution environment for Kotlin/Spring Boot applications on ppc64le | Standard für Kotlin/Java. IBM Semeru bietet optimierte Builds für Power-Architektur. JDK 21 als LTS.     |
| **Frameworks** | Spring Boot             | 3.4.5                                  | Core application framework for backend modules and services     | Umfassend, etabliert, unterstützt schnelle Entwicklung, gute Integrationen. Von PRD vorgegeben. |
|                      | Axon Framework          | 4.11.2 \<br/\>(Upgrade auf v5 geplant)     | Framework for DDD, CQRS, Event Sourcing                     | Spezialisiert auf die gewählten Architekturmuster, gute Integration mit Spring. Von PRD vorgegeben. |
|                      | React                   | 19.1                                   | JavaScript library for building the Control Plane UI          | Populär, komponentenbasiert, großes Ökosystem. Von PRD vorgegeben.                          |
| **Databases** | PostgreSQL              | 17.5                                   | Primary RDBMS for Event Store, Read Models, and State Data | Leistungsstark, Open Source, ACID-konform, gute Unterstützung für JSON. Von PRD vorgegeben.    |
| **Build Tool** | Gradle                  | 8.14                                   | Build automation for the monorepo                           | Flexibel, gut für Kotlin & Multi-Projekt-Builds, Dependency Management. Von PRD vorgegeben. |
| **Infrastructure** | Docker                  | Neueste stabile Engine-Version         | Containerization for deployment and development consistency on ppc64le | Ermöglicht portable Umgebungen, vereinfacht Deployment. Erwähnt in PRD NFRs.                   |
|                      | Docker Compose          | Neueste stabile Version                | Orchestration of local development/test containers (App, DB, etc.) and production stack on single VM | Vereinfacht das Setup von Multi-Container-Umgebungen lokal und in Produktion (gemäß Anforderung). |
| **UI Libraries (Control Plane)** | React-Admin             | 5.8.1                                  | Admin framework for building the Control Plane UI (data grids, forms, etc.) | Bietet viele Out-of-the-Box-Komponenten für Admin-Oberflächen, inspiriert Design laut PRD. |
| **State Management (Control Plane)** | React-Admin built-in / recommended (e.g., React Context, Ra-Store) | Gekoppelt an React-Admin Version        | Frontend state management for Control Plane UI                | Von React-Admin bereitgestellt oder direkt integrierbar.                                 |
| **Testing (Backend)** | JUnit Jupiter           | 5.12.2                                 | Testing framework for Java/Kotlin                           | Standard für JVM-Tests.                                                                  |
|                      | MockK                   | 1.14.2                                 | Mocking library for Kotlin                                  | Kotlin-idiomatische Mocks.                                                               |
|                      | Kotest                  | 5.9.1                                  | Assertion library for fluent tests                        | Verbessert Lesbarkeit von Tests, bietet verschiedene Teststile.                         |
|                      | Testcontainers          | 1.21.0 (Java)                          | For integration tests with Dockerized dependencies (e.g., DB) | Ermöglicht zuverlässige Integrationstests.                                                |
|                      | Axon Test Fixture       | Gekoppelt an Axon Framework 4.11.2     | For testing Axon aggregates and sagas                       | Spezifisch für Axon-Komponenten.                                                         |
| **Testing (Frontend - CP UI)** | Jest                    | 29.7.0                                 | JavaScript testing framework                                | Weit verbreitet für React-Tests.                                                         |
|                      | React Testing Library   | 16.3.x                                 | For testing React components                                | Fördert Best Practices beim Testen von UI-Komponenten.                                    |
|                      | Playwright              | 1.52.x                                 | For End-to-End testing of the Control Plane UI            | Mächtiges Werkzeug für zuverlässige E2E-Tests.                                         |
| **CI/CD** | GitHub Actions          | N/A (Service)                          | Automation for build, test, and deployment pipelines        | Integriert in GitHub, flexibel konfigurierbar. Gemäß Projektstruktur.                     |
| **Other Tools** | Logback                 | (Version via Spring Boot)              | Logging framework for backend                               | Standard in Spring Boot.                                                                 |
|                      | Micrometer              | (Version via Spring Boot)              | Application metrics facade                                  | Ermöglicht Metrik-Export (z.B. an Prometheus). Erwähnt in PRD.                           |
|                      | springdoc-openapi       | `Version passend zu Spring Boot 3.4.x` | Generates OpenAPI 3 documentation from Spring Boot controllers | Automatisiert API-Dokumentationserstellung.                                              |
|                      | Liquibase               | 4.31.1                                 | Tool for managing database schema changes (Read Models, etc.) | Notwendig für versionierte DB-Migrationen.                                               |

## 11. Infrastructure and Deployment Overview

This section outlines the infrastructure environment for which the ACCI EAF and its derived applications are designed, along with the strategy for their deployment.

* **Target Infrastructure Provider(s):**
  * On-premise Data Centers / Private Cloud environments.
  * **Virtual Machines (VMs) are provided and managed by the customer**, typically running on IBM Power Architecture (ppc64le). ACCI EAF deployment does not include VM provisioning or OS-level configuration beyond what is necessary for Docker runtime.
  * No dependency on public cloud provider services (e.g., AWS, Azure, GCP) or Kubernetes for the core EAF and its MVP applications.

* **Core Services Utilized (deployed via Docker Compose on a single customer VM):**
  * The entire application stack, including all necessary services, is designed to run orchestrated by Docker Compose on a single VM provided by the customer. This includes:
    * **PostgreSQL Server:** Runs as a Docker container, managed within the Docker Compose setup. Its data will be persisted using Docker volumes mapped to the host VM.
    * **ACCI EAF Applications:** Runnable applications like `eaf-controlplane-api` and `eaf-license-server`, as well as applications built by end-users based on the ACCI EAF, will be deployed as Docker containers managed by Docker Compose.
    * **Web Server / Reverse Proxy (Optional):** If required (e.g., for SSL termination, serving static content for the Control Plane UI, or as an API gateway), a web server like Nginx or Traefik would also run as a Docker container within the same Docker Compose setup.

* **Infrastructure Definition & Application Packaging:**
  * **Application Packaging:** **Docker** is used to create container images for all runnable EAF components and PostgreSQL. All Docker images are built specifically for the **ppc64le** architecture. Dockerfiles will be maintained within each application module's source code.
  * **Runtime Orchestration on VM:** **Docker Compose** is the primary tool for defining, orchestrating, and managing the lifecycle of the entire application stack (database, backend applications, web server) on the customer's VM. A master `docker-compose.yml` file will define all services, networks, volumes, and configurations.
  * **VM Provisioning & Configuration:** This is the **responsibility of the customer**. The ACCI EAF deployment package assumes a VM with a compatible Linux OS, Docker (and Docker Compose) installed and running, and sufficient resources. No tools like Ansible for VM configuration are provided or required by ACCI for the EAF deployment itself.
  * **Delivery Package (for Air-Gapped Environments):**
    * For customer deployments, which are always considered air-gapped and without access to public Docker registries, ACCI will provide a **TAR ball**.
    * This TAR ball will contain:
      * All required Docker images (exported using `docker save`).
      * The `docker-compose.yml` file defining the entire stack.
      * Necessary helper scripts (e.g., Bash or Python) for installation, updates, and basic management (start, stop, status) of the stack.
      * Liquibase migration scripts for the database.
      * Configuration template files.

* **Deployment Strategy:**
  * **Artifacts:** The CI/CD pipeline (GitHub Actions) will build, test, and package the ppc64le Docker images. The final "release artifact" for customers is the aforementioned TAR ball.
  * **CI/CD Tool:** **GitHub Actions** for Continuous Integration, automated testing, and assembling the deployment TAR ball.
  * **Deployment to Customer VM (Air-Gapped Manual Process):**
        1. Secure transfer of the versioned TAR ball to the customer's environment.
        2. The customer (or an ACCI engineer on-site) unpacks the TAR ball on the target VM.
        3. The provided installation script is executed. This script will typically:
            *Load Docker images into the local Docker daemon on the VM (using `docker load < image.tar`).
            * Configure environment-specific parameters (e.g., network settings, external service URLs if any, secrets – potentially via a `.env` file used by Docker Compose).
            *Run database schema migrations using Liquibase (this might be integrated into an application's startup script or run as a separate step by the install script before starting the main application stack).
            * Start the entire application stack using `docker-compose up -d` with the provided `docker-compose.yml`.
  * **Updates:** Updates follow a similar process: deliver a new TAR ball, stop the current stack, load new images, potentially run data/schema migrations, and restart the stack with the updated configuration/images.

* **Environments:**
  * **Development:** Local developer machines using Docker Compose to accurately replicate the single-VM production setup.
  * **Staging/QA:** A dedicated VM environment for integration testing, UAT, and performance testing, deployed using the same TAR ball and Docker Compose methodology as production.
  * **Production:** The customer's live VM, deployed and managed as described above.

* **Environment Promotion Strategy:**
  * Code is developed and tested. Upon successful validation, a release candidate TAR ball is built.
  * This TAR ball is first deployed to the **Staging/QA** environment for thorough testing.
  * After successful Staging validation and sign-off, the *identical* TAR ball is approved for **Production** deployment by the customer.

* **Rollback Strategy:**
  * **Application Stack Rollback:** In case of a faulty deployment, the primary rollback strategy is to:
        1. Stop and remove the current Docker Compose stack (`docker-compose down`).
        2. If a previous version's TAR ball and loaded images are still available on the VM (or can be re-transferred), use the scripts and `docker-compose.yml` from that previous version to restart the older, stable stack.
        3. Careful management of Docker image tags within the TAR ball (e.g., `image:tag_version_X`) and corresponding `docker-compose.yml` files is crucial.
  * **Database Rollback:** Liquibase supports rollback commands for schema changes. Data state rollbacks would typically require restoring from a database backup. Procedures for database backup are the customer's responsibility but can be advised by ACCI.

## 12. Error Handling Strategy

A robust error handling strategy is crucial for the stability, maintainability, and diagnosability of the ACCI EAF and applications built upon it. This section outlines the general approach, logging practices, and specific error handling patterns.

* **General Approach:**
  * **Exceptions as Primary Mechanism:** Exceptions will be the primary mechanism for signaling and propagating errors within the application code. Kotlin's standard exceptions and Java's exception hierarchy will be used.
  * **Custom Exception Hierarchy:** A custom exception hierarchy will be defined, extending standard exceptions (e.g., `RuntimeException`, `IllegalArgumentException`). This hierarchy will include:
    * A base application exception (e.g., `AcciEafException`).
    * Specific business exceptions related to different domains (e.g., `TenantNotFoundException` from `eaf-multitenancy`, `UserAuthenticationException` from `eaf-iam`, `LicenseValidationException` from `eaf-licensing`).
    * Technical/integration exceptions (e.g., `ExternalServiceUnavailableException`, `ConfigurationException`).
  * **Clear Error Messages:** Exceptions should carry clear, concise messages intended for developers/logs, and potentially unique error codes for easier tracking and reference.
  * **Fail Fast:** For unrecoverable errors or invalid states, the system should fail fast to prevent further inconsistent processing.

* **Logging:**
  * **Library/Method:** **Logback** (provided by default with Spring Boot) will be the primary logging framework. It will be configured for **structured logging in JSON format** to facilitate easier parsing, searching, and analysis by log management systems. The `logstash-logback-encoder` library can be used to enhance JSON formatting and include custom fields.
  * **Log Levels:** Standard log levels will be used consistently:
    * `ERROR`: Critical errors that prevent normal operation or lead to data inconsistency. Significant failures requiring immediate attention. Includes stack traces.
    * `WARN`: Potential problems or unusual situations that do not (yet) halt processing but might indicate future issues or require investigation (e.g., retrying an operation, configuration issues, deprecation warnings).
    * `INFO`: High-level messages tracking the application's lifecycle and significant business operations (e.g., application startup, major service calls, tenant creation, successful license activation).
    * `DEBUG`: Fine-grained information useful for developers during debugging (e.g., method entry/exit, variable values, detailed flow tracing). Should be disabled in production by default but configurable.
    * `TRACE`: Extremely detailed diagnostic information, typically only enabled for specific troubleshooting scenarios.
  * **Contextual Information in Logs:** All log entries (especially `INFO`, `WARN`, `ERROR`) should include crucial contextual information:
    * **Timestamp** (ISO 8601 format).
    * **Log Level**.
    * **Thread Name**.
    * **Logger Name** (typically the class name).
    * **Message**.
    * **Stack Trace** (for exceptions at `ERROR` and optionally `WARN` level).
    * **Correlation ID (Trace ID):** A unique ID generated at the start of a request (e.g., incoming API call) and propagated through all subsequent service calls and log messages related to that request. This is critical for tracing distributed operations. Spring Cloud Sleuth (even without Zipkin for tracing if not used) or a similar mechanism (e.g., MDC) will be used.
    * **Tenant ID:** (If applicable to the context and not sensitive in the log message itself).
    * **User ID / Principal Name:** (If applicable, ensuring PII is handled according to security policies).
    * **Operation Name / Service Name:** Identifying the specific operation or component.
    * **Key Parameters (Sanitized):** Relevant input parameters or identifiers, ensuring sensitive data (passwords, secrets, PII) is masked or omitted.

* **Specific Handling Patterns:**
  * **External API Calls / Integrations (HTTP, LDAP, SMTP, etc.):**
    * **Timeouts:** Configure appropriate connection and read timeouts for all external calls to prevent indefinite blocking. Libraries like `OkHttp`, `RestTemplate` (with configuration), or specific protocol libraries (e.g., JavaMail, UnboundID LDAP SDK) provide mechanisms for this.
    * **Retries:** For transient network issues or temporary unavailability of external services, implement automatic retry mechanisms with exponential backoff and jitter. **Spring Retry** (`@Retryable`) is the preferred library for this.
    * **Circuit Breakers:** For integrations that are prone to failures or high latency, a Circuit Breaker pattern will be implemented using **Resilience4j**. This prevents cascading failures by stopping requests to a failing service for a period. Fallback mechanisms (e.g., returning cached data, default values, or a specific error response) should be considered.
    * **Error Mapping:** Errors from external services (e.g., HTTP 4xx/5xx status codes, LDAP error codes, SMTP exceptions) will be caught and mapped to specific internal `AcciEafException` subtypes, providing a consistent error handling approach within the EAF. Sensitive details from external errors should not be directly exposed to end-users.
  * **Internal Business Logic Exceptions:**
    * Custom domain-specific exceptions (e.g., `InvalidTenantStatusException`, `DuplicateUsernameException`, `LicenseExpiredException`) will be thrown by business logic in aggregates or domain services.
    * **API Layer Error Handling (e.g., in `eaf-controlplane-api`, `eaf-license-server`):** Spring Boot's `@ControllerAdvice` and `@ExceptionHandler` mechanisms will be used to globally handle these custom exceptions (and standard Spring exceptions). These handlers will:
      * Log the full error with stack trace at `ERROR` level.
      * Transform the exception into a standardized JSON error response for the API client, including a user-friendly message, a unique error code/ID (for support), and appropriate HTTP status code (e.g., 400 for validation errors, 404 for not found, 403 for forbidden, 409 for conflicts, 500 for unexpected server errors).
  * **Axon Framework Command Handling:**
    * Exceptions thrown by `@CommandHandler` methods in Aggregates will be propagated back to the `CommandGateway` caller.
    * These exceptions should be specific business exceptions. The API layer (or service layer dispatching the command) will then handle these as described above.
    * Axon also allows for `CommandDispatchInterceptor` and `CommandHandlerInterceptor` to add cross-cutting error handling if needed.
  * **Axon Framework Event Handling / Projections:**
    * Errors occurring within `@EventHandler` methods (e.g., when updating read models) require careful consideration. Axon Framework provides configurable error handlers for event processors (e.g., `ListenerInvocationErrorHandler`, `ErrorHandler`).
    * **Strategy:**
      * For transient errors (e.g., database connection issue during read model update), a retry mechanism might be configured for the event processor.
      * For non-transient errors (e.g., an event that consistently fails to be processed due to a bug in the handler or unexpected data), the event should typically be moved to a Dead-Letter Queue (DLQ) after a few failed attempts, or the event processor might be stopped to prevent blocking further event processing. This requires monitoring of the DLQ or processor status.
      * Logging of such event processing failures is critical.
  * **Transaction Management:**
    * **Local Transactions (e.g., Read Model Updates):** Standard Spring `@Transactional` annotations will be used to manage ACID transactions for database operations within event projectors or services interacting directly with PostgreSQL for stateful data. If an event handler processes multiple updates, these should ideally be within a single transaction.
    * **Distributed Transactions / Sagas (Consistency across Aggregates):** For business processes that span multiple aggregates and require eventual consistency, **Axon Sagas** will be used. Sagas listen to events and dispatch new commands to orchestrate the process. Sagas must implement compensation logic (compensating actions/commands) to handle failures in any step of the distributed transaction, ensuring the system can be brought back to a consistent state.

## 13. Coding Standards (ACCI Kotlin Coding Standards v1.0)

These standards, collectively referred to as the **"ACCI Kotlin Coding Standards v1.0"**, are mandatory for all code generated by AI agents and human developers for the ACCI EAF project. Deviations are not permitted unless explicitly approved and documented as an exception in this section or a linked addendum. Adherence to these standards will be enforced through code reviews and automated checks in the CI/CD pipeline where possible using tools like Ktlint and Detekt.

The primary goals of these standards are to ensure code quality, consistency, maintainability, readability, and to provide clear guidelines for efficient development.

* **Primary Language & Runtime(s):**
  * **Language:** Kotlin (Version `2.1.21` as specified in "Definitive Tech Stack Selections").
  * **Runtime:** JVM (Java `21` LTS - e.g., IBM Semeru Runtimes for Power or OpenJDK, as specified in "Definitive Tech Stack Selections").

* **Style Guide & Linter:**
  * **Official Kotlin Coding Conventions:** All Kotlin code must adhere to the official Kotlin Coding Conventions documented by JetBrains: [https://kotlinlang.org/docs/coding-conventions.html](https://kotlinlang.org/docs/coding-conventions.html)
  * **Linter/Formatter:**
    * **Ktlint:** Ktlint will be used as the primary linter and formatter to enforce these conventions. It should be integrated into the build process (Gradle task) and ideally into the IDE.
    * **IDE Formatting:** IntelliJ IDEA's built-in formatter should be configured to match the official Kotlin Coding Conventions and Ktlint rules (often achieved by importing settings from `.editorconfig` if Ktlint is configured to use it).
  * **Configuration:** A shared `.editorconfig` file will be maintained at the root of the monorepo, containing settings for Ktlint and general editor configurations (indentation, line endings, etc.). Specific Ktlint rule set configurations, if deviating from defaults or adding custom rules, will be documented and managed centrally.
  * **CI Enforcement:** The CI/CD pipeline must include a step to run Ktlint checks; builds should fail if violations are detected.
  * **Static Analysis (Detekt):** Detekt will be used in addition to Ktlint for deeper static analysis, checking for code smells, complexity issues, and potential bugs. Detekt will also be integrated into the Gradle build and CI pipeline, with generated reports. Build failure can be configured for critical issues.

* **Naming Conventions:**
    (Adhering to Kotlin's standard conventions)
  * **Packages:** Lowercase with dot separation (e.g., `com.axians.accieaf.iam.domain`).
  * **Classes, Interfaces, Objects, Enums, Annotations, Type Aliases:** `PascalCase` (e.g., `TenantService`, `UserRepository`).
  * **Functions, Methods, Properties (non-constant), Local Variables, Parameters:** `camelCase` (e.g., `createTenant`, `userName`, `isActive`).
  * **Test Functions/Methods:** `camelCase` or descriptive sentences in backticks (e.g., `` `should create tenant when valid data is provided` ``).
  * **Constants (`const val`, top-level `val` with `@JvmField` in objects, `enum` entries):** `UPPER_SNAKE_CASE` (e.g., `MAX_RETRIES`, `DEFAULT_TIMEOUT_MS`).
  * **Generic Type Parameters:** Single uppercase letter (e.g., `T`, `R`) or a descriptive `PascalCase` name if more clarity is needed (e.g., `RequestType`).
  * **Kotlin Source Files (`.kt`):**
    * If a file contains a single top-level class/object/interface, the filename must match its name with the `.kt` extension (e.g., `Tenant.kt`).
    * If a file contains multiple top-level declarations or only extension functions/properties, the filename should be descriptive of its content in `PascalCase` (e.g., `CollectionUtils.kt`).
  * **Gradle Modules:** `kebab-case` as defined in the "Project Structure" (e.g., `eaf-core`, `eaf-iam`).

* **File Structure:**
  * **General Project Structure:** Adhere strictly to the layout defined in the "Project Structure" section of this document.
  * **Package Structure within Modules:** Within each module's `src/main/kotlin/` and `src/test/kotlin/`, packages should be organized by feature or layer, consistent with Hexagonal Architecture principles (e.g., `com.axians.accieaf.[moduleName].domain`, `com.axians.accieaf.[moduleName].application`, `com.axians.accieaf.[moduleName].adapter.api`, `com.axians.accieaf.[moduleName].adapter.persistence`).
  * **Unit Test File Organization:**
    * **Location:** Unit test files must be located in the `src/test/kotlin/` directory of their respective module. The package structure within `src/test/kotlin/` must mirror the package structure of the code being tested in `src/main/kotlin/`.
    * **Naming Convention:** Test class files must be named after the class they are testing, appended with `Test`. For example, a class `com.axians.accieaf.iam.application.TenantService` located in `eaf-iam/src/main/kotlin/com/axians/accieaf/iam/application/TenantService.kt` will have its corresponding test class as `com.axians.accieaf.iam.application.TenantServiceTest` in `eaf-iam/src/test/kotlin/com/axians/accieaf/iam/application/TenantServiceTest.kt`.

* **Asynchronous Operations:**
  * **Kotlin Coroutines:** Kotlin Coroutines (`suspend` functions, `kotlinx.coroutines.flow.Flow`, `kotlinx.coroutines.channels.Channel`) are the preferred mechanism for managing asynchronous operations, non-blocking I/O, and concurrency.
  * **Structured Concurrency:** Code must adhere to the principles of structured concurrency. Coroutines should be launched within a `CoroutineScope` that is tied to the lifecycle of the component managing them (e.g., a Spring service, an Axon event handler). Avoid launching coroutines on `GlobalScope` unless explicitly justified and managed.
  * **Dispatchers:** Use appropriate dispatchers from `kotlinx.coroutines.Dispatchers` (`Dispatchers.IO` for blocking I/O operations that are not yet adapted for coroutines, `Dispatchers.Default` for CPU-intensive work, `Dispatchers.Unconfined` with caution). Spring MVC controllers with `suspend` functions will typically run on an appropriate dispatcher managed by Spring.
  * **Axon Framework Integration:** Leverage Axon Framework's support for Kotlin Coroutines (e.g., suspending command handlers, query handlers, and event handlers where provided by `axon-kotlin` extensions or through appropriate integration).
  * **Spring Integration:** Spring Framework (including Spring Boot MVC) provides excellent support for Kotlin Coroutines. `suspend` functions can be used directly in `@RestController` methods.
  * **Error Handling:** Ensure proper error handling within coroutines using `try-catch` blocks and an understanding of coroutine cancellation.
  * **Avoid Blocking:** Do not call blocking code from within a coroutine context without switching to an appropriate dispatcher (e.g., `Dispatchers.IO`).

* **Type Safety:**
  * **Leverage Kotlin's Type System:** Kotlin's strong type system, including its robust null-safety features, must be utilized to its full extent.
    * **Null-Safety:**
      * Declare types as nullable (`?`) only when `null` is a valid and meaningful value for that variable or property.
      * Avoid the non-null assertion operator (`!!`) wherever possible. Its use is strongly discouraged and requires explicit justification in a comment if deemed absolutely necessary.
      * Prefer safe calls (`?.`), the Elvis operator (`?:`), `let` with safe calls, or other idiomatic Kotlin constructs for handling nullable types.
    * **Explicit Types for Public APIs:** Publicly exposed functions, properties, and class members (i.e., those not `private` or `internal`) must have explicit type declarations for parameters and return types. This improves code clarity and maintainability, even if the type could be inferred by the compiler. For `private` or `internal` members, type inference is acceptable if it enhances readability.
  * **Policy on `Any`:** The use of `kotlin.Any` as a type for parameters, properties, or return values should be avoided. Prefer specific types or generics (`<T>`) to maintain type safety and clarity.
  * **Type Definitions (Location and Style):**
    * **Domain Objects:** Data classes, sealed classes/interfaces, and enums representing core domain concepts (Aggregates, Entities, Value Objects, Domain Events, Commands, Queries) should typically be defined within the `domain` package (or a sub-package thereof) of their respective module (e.g., `eaf-iam/src/main/kotlin/com/axians/accieaf/iam/domain/model/User.kt`).
    * **DTOs (Data Transfer Objects):** DTOs used for API request/response payloads or for transferring data between layers should be clearly defined as data classes. They are typically located in `dto` or `model` sub-packages within the relevant adapter layer (e.g., `eaf-controlplane-api/src/main/kotlin/com/axians/accieaf/controlplane/adapter/api/dto/TenantDto.kt`) or application service layer.
    * **Immutability:** Prefer immutable types (data classes with `val` properties, `List`, `Set`, `Map` over their mutable counterparts) where practical.

* **Comments & Documentation:**
  * **KDoc (Kotlin Documentation):**
    * **Mandatory for Public APIs:** All `public` and `internal` top-level declarations (classes, interfaces, objects, functions, properties) and their members (constructors, functions, properties) must have KDoc documentation.
    * **Content:** KDoc should clearly explain the purpose of the element, its parameters (`@param`), return values (`@return`), and any exceptions it might throw (`@throws`). For classes, describe their responsibility and key features.
    * **Clarity over Verbosity:** Explain the *why* behind complex logic or non-obvious design decisions, not just *what* the code does (which should be evident from well-written, self-documenting code).
    * **Avoid Redundant Comments:** Do not comment on obvious code (e.g., simple getters/setters that only get/set a field).
  * **Inline Comments:** Use inline comments (`//`) sparingly to clarify complex or tricky sections of code that cannot be made self-evident through better naming or structure.
  * **`TODO` / `FIXME` Comments:** Use standard `// TODO:` or `// FIXME:` comments to mark areas requiring future attention, known issues, or temporary workarounds. If possible, include a reference to a tracking item (e.g., JIRA ticket ID), your initials, and the date. Example: `// TODO (MAX-123, 2025-05-16): Refactor this to use the new FoobarService.`
  * **Module `README.md` Files:** Each Gradle module (e.g., `eaf-core`, `eaf-iam`) must have a `README.md` file at its root. This README should briefly explain:
    * The purpose and main responsibilities of the module.
    * Key architectural decisions or patterns specific to the module (if any).
    * How to build and test the module (if there are specific instructions beyond standard root Gradle commands).
    * Any important dependencies or setup instructions for developers working on this module.
  * **Architectural Decision Records (ADRs):** Significant architectural decisions, especially those with non-obvious trade-offs or long-term implications, should be documented using Architectural Decision Records (ADRs). ADRs should be stored in a dedicated `docs/adr/` directory in the monorepo, using a simple format (e.g., Markdown with fields like Title, Status, Context, Decision, Consequences).

* **Dependency Management:**
  * **Tool:** Gradle with Version Catalogs (typically defined in `gradle/libs.versions.toml` at the project root, and potentially referenced/managed via `build-logic`) is the single source of truth for all external library versions.
  * **Policy on Adding New Dependencies:** Adding new external dependencies (especially to core EAF modules) requires careful consideration:
    * **Justification:** The need for the dependency must be clearly articulated. Could the functionality be achieved with existing dependencies or Kotlin/Java standard library features?
    * **Alternatives Research:** Briefly document considered alternatives and why the chosen dependency is preferred.
    * **License Compatibility:** Verify the dependency's license is compatible with ACCI EAF's overall licensing strategy and distribution model. Permissive licenses like Apache 2.0, MIT, or EPL are generally preferred. GPL/LGPL dependencies require thorough review and approval due to their reciprocal nature.
    * **Security Vulnerabilities:** Check the dependency (and its transitive dependencies) for known security vulnerabilities using tools like the OWASP Dependency-Check Gradle plugin, Snyk, or GitHub Dependabot alerts.
    * **Maturity & Maintenance:** Prefer well-maintained, stable libraries from reputable sources with an active community and good documentation. Avoid deprecated or unmaintained libraries.
    * **Transitive Dependencies:** Analyze the impact of transitive dependencies introduced by the new library. Minimize unnecessary transitive dependencies.
    * **ppc64le Compatibility:** For libraries that might include native code, ensure compatibility with the ppc64le target architecture. Pure Java/Kotlin libraries are generally safe.
    * **Approval:** For core EAF modules (`eaf-*`), adding a new external dependency may require review and approval from a lead architect or technical lead.
  * **Versioning Strategy (in Version Catalog):**
    * **Pinned Versions:** Use specific, pinned versions for all dependencies. Avoid dynamic versions (e.g., `+`, `latest.release`, version ranges like `[1.0, 2.0)`) to ensure reproducible and stable builds.
    * **Regular Updates:** Plan for regular, controlled updates of dependencies to incorporate security patches and improvements. Such updates must be tested thoroughly.
  * **Dependency Scopes:** Use appropriate Gradle dependency configurations (`implementation`, `api`, `compileOnly`, `runtimeOnly`, `testImplementation`, etc.) to correctly manage compile-time and runtime classpaths and to avoid leaking transitive dependencies unnecessarily from modules' APIs. Use `api` sparingly and only when a module intentionally exposes types from a dependency as part of its own public API.

### 13.1 Detailed Language & Framework Conventions

This subsection provides specific conventions and best practices for the primary technologies used in the ACCI EAF: Kotlin, Spring Boot, and Axon Framework. Adherence to these guidelines is mandatory.

#### 13.1.1 Kotlin Specifics

* **Immutability:**
  * **Prefer `val` over `var`:** Declare variables and properties as immutable (`val`) by default. Use `var` only when mutability is essential and clearly justified.
  * **Immutable Collections:** Use Kotlin's immutable collection types (`List`, `Set`, `Map` created via `listOf()`, `setOf()`, `mapOf()`) by default. For collections that need to be modified, clearly define if a mutable version (`MutableList`, etc.) is necessary or if a new immutable collection should be created from an existing one (copy-on-write).
  * **Data Classes:** Properties in `data class`es should predominantly be declared with `val`. If an object needs to be "changed", create a new instance using the `copy()` method with the modified properties.
  * **Persistent Collections:** For scenarios requiring high-performance immutable collections with efficient modification operations, consider using Kotlin's experimental persistent collections library (`kotlinx.collections.immutable`) after careful evaluation.

* **Functional vs. OOP Programming:**
  * **Balanced Approach:** Kotlin excels at both paradigms. Employ a balanced approach:
    * **OOP:** Use classes, interfaces, and objects to define clear entities (especially DDD Aggregates), domain services, application services, and components with well-defined responsibilities and encapsulated state.
    * **Functional:** Leverage Kotlin's first-class functions, lambdas, higher-order functions, extension functions, and rich collection processing API (`map`, `filter`, `fold`, `flatMap`, sequences, etc.) for data transformations, concise business logic where appropriate, and to reduce boilerplate code.
  * **Pure Functions:** Prefer pure functions (functions without side effects whose output depends only on their input) where possible, as they are easier to reason about, test, and parallelize.

* **Error Handling Specifics (Kotlin):**
  * **Exceptions:** Continue using custom exceptions as defined in the general "Error Handling Strategy".
  * **`Result<T>` Type:** For functions within the domain or application layers that can fail in a predictable and recoverable manner (not representing system-level exceptions), consider returning Kotlin's `kotlin.Result<T>` type or a custom sealed class hierarchy to represent success/failure outcomes explicitly. This forces callers to handle both paths. Avoid overusing this for every possible failure; exceptions are appropriate for unexpected or truly exceptional situations.
  * **`try-catch` Expressions:** Utilize Kotlin's `try-catch` as an expression where it improves conciseness, e.g., `val result = try { operation() } catch (e: SpecificException) { fallbackValue }`.
  * **Coroutines Error Handling:** Ensure proper error handling in coroutines, including understanding of cancellation and the difference between `supervisorScope` and `coroutineScope` for isolating failures.

* **Null Handling (Kotlin):**
  * **Embrace Null Safety:** Fully utilize Kotlin's built-in null-safety features. Clearly define nullability in all type signatures (parameters, return types, properties).
  * **Avoid `!!`:** The non-null assertion operator (`!!`) is strongly discouraged. Its use must be a rare exception, explicitly justified with a comment explaining why nullability is impossible at that point.
  * **Idiomatic Null Handling:** Prefer safe calls (`?.`), the Elvis operator (`?:`), safe casts (`as?`), and scope functions (`let`, `run`, `also`, `apply`) with safe calls for handling nullable values gracefully.

* **Visibility Modifiers:**
  * Use Kotlin's visibility modifiers (`public`, `internal`, `protected`, `private`) diligently to encapsulate implementation details and expose clear APIs for modules and classes.
  * **`internal`:** This modifier is particularly useful for declarations that should be accessible anywhere within the same Gradle module but not from other modules, aiding in modular design.
  * Default visibility is `public`; be explicit if a more restrictive visibility is intended.

* **Logging Specifics (Kotlin):**
  * **Logger Instantiation:** Obtain an SLF4J logger instance using: `private val logger = LoggerFactory.getLogger(YourClass::class.java)` or `private val logger = LoggerFactory.getLogger(javaClass)` within a class.
  * **Parameterized Logging:** Always use parameterized logging for SLF4J (e.g., `logger.info("User {} processed action {} with result: {}", userId, action, result)`) instead of string concatenation to improve performance and readability.
  * **MDC (Mapped Diagnostic Context):** Ensure crucial contextual information (Correlation ID, Tenant ID, User ID - if safe) is placed into the MDC at the beginning of a request or operation so that it's automatically included in structured JSON log output by Logback.

#### 13.1.2 Spring Boot Specifics

* **Dependency Injection:**
  * **Constructor Injection:** Exclusively use constructor-based dependency injection for all Spring components (`@Service`, `@Component`, `@RestController`, `@Repository`, `@Configuration`). This promotes immutability for dependencies and makes components easier to test. Field injection (`@Autowired` on fields) is disallowed.
* **Configuration:**
  * **`@ConfigurationProperties`:** Use type-safe configuration properties by defining Kotlin `data class`es annotated with `@ConfigurationProperties` and `@Configuration`) to bind values from `application.yml` or environment variables. Avoid using `@Value` for individual properties where a group of related properties can be managed by a configuration properties class.
  * **Externalized Configuration:** Follow Spring Boot's conventions for externalized configuration, allowing properties to be overridden by profiles, environment variables, or command-line arguments.
* **Stereotype Annotations:**
  * Consistently use Spring's stereotype annotations (`@Service` for business logic, `@Component` for generic components, `@Repository` for data access layers (if not using Axon repositories exclusively), `@RestController` for API endpoints, `@Configuration` for configuration classes).
* **Transactions (`@Transactional`):**
  * Apply `@Transactional` (from `org.springframework.transaction.annotation`) to service methods that perform database operations requiring transactional consistency (e.g., read model updates, saving configuration data).
  * Understand and correctly use transaction propagation levels (e.g., `REQUIRED`, `REQUIRES_NEW`) and the `readOnly` flag where appropriate.
  * For Axon-managed aggregates, transactions are typically handled by Axon around command processing and event persistence. `@Transactional` might be more relevant for event handlers updating separate read models or for services that don't directly involve Axon aggregates.
* **Spring Security:**
  * Adhere to Spring Security best practices for configuring authentication (e.g., integration with `eaf-iam` for OIDC/SAML/LDAP) and authorization.
  * Utilize method security (`@PreAuthorize`, `@PostAuthorize`, `@Secured`) for fine-grained access control on service methods where appropriate, based on roles and permissions defined in `eaf-iam`.
* **REST Controllers (`@RestController`):**
  * Strive to return `ResponseEntity<T>` from controller methods to have full control over the HTTP response status, headers, and body.
  * Use appropriate HTTP verbs (GET, POST, PUT, DELETE, PATCH) semantically.
  * Implement proper request validation using Spring Validation API (Bean Validation with annotations like `@Valid`, `@NotNull`, `@Size` on DTOs).
  * Use Kotlin `suspend` functions in controllers for non-blocking request handling when I/O operations are involved in the backend.

#### 13.1.3 Axon Framework Specifics

* **Aggregates (`@Aggregate`):**
  * **Granularity:** Design aggregates to be small and focused on a single consistency boundary. An aggregate should encapsulate state and business logic that must be atomically consistent.
  * **State Modification:** Aggregate state must *only* be modified by methods annotated with `@EventSourcingHandler`, reacting to events applied by the aggregate itself.
  * **Command Handlers (`@CommandHandler`):** These methods validate incoming commands against the aggregate's current state and business rules. If valid, they apply one or more domain events using `AggregateLifecycle.apply(event)`. Command handlers should be lean and focused on decision-making.
  * **Identifiers:** Use `@AggregateIdentifier` correctly.
  * **Immutability:** Strive for immutable state within aggregates where possible (using `val` properties updated via event sourcing handlers creating new state objects or using immutable collections).
* **Commands, Events, Queries:**
  * **Immutability:** These messages (Commands, Events, Queries, and their results) must be immutable. Kotlin `data class`es with `val` properties are ideal for this purpose.
  * **Naming Conventions:**
    * Commands: Imperative mood, describe the action (e.g., `CreateTenantCommand`, `ActivateLicenseCommand`).
    * Events: Past tense, describe a fact that has occurred (e.g., `TenantCreatedEvent`, `LicenseActivatedEvent`).
    * Queries: Descriptive nouns or verb phrases indicating the data being requested (e.g., `FindTenantByIdQuery`, `ListActiveUsersQuery`).
  * **Versioning:** Plan for event and query model versioning early if significant evolution is expected. Axon provides mechanisms for upcasting events.
* **Event Processors (`@EventHandler`, `@SagaEventHandler`):**
  * **Tracking Event Processors:** Use Tracking Event Processors by default for event handlers that update read models or trigger Sagas. They offer better resilience, scalability, and allow replaying events.
  * **Error Handling:** Implement robust error handling for event processors using Axon's `ListenerInvocationErrorHandler` and `ErrorHandler` configurations. Consider strategies like retries for transient errors and Dead-Letter Queues (DLQs) for unrecoverable errors to prevent blocking event streams.
  * **Idempotency:** Design event handlers to be idempotent, as events might be redelivered under certain failure or replay scenarios.
* **Sagas (`@Saga`):**
  * Use Sagas to manage complex business transactions that span multiple aggregates and require eventual consistency.
  * Clearly define saga start conditions (`@StartSaga` on an event handler) and end conditions (`@EndSaga`).
  * Implement compensation logic (dispatching compensating commands) to handle failures in any step of the saga, ensuring the system can be brought back to a consistent state or a defined error state.
  * Associate sagas with relevant data using `@SagaEventHandler(associationProperty = "...")`.
* **Query Handlers (`@QueryHandler`):**
  * Implement query handlers to answer queries dispatched through the `QueryGateway`. These handlers typically fetch data from read models.
  * Ensure query handlers are efficient and only retrieve the data necessary for the query.

#### 13.1.4 Key Library Usage Conventions (General Kotlin/Java)

* **Kotlin Standard Library:** Make extensive use of the Kotlin standard library's rich features for collection processing (e.g., `map`, `filter`, `firstOrNull`, `groupBy`), scope functions (`let`, `run`, `apply`, `also`, `with`), string manipulation, and other utilities.
* **Java Time API (`java.time.*`):** Exclusively use the Java Time API (JSR 310) for all date and time representations and manipulations. This includes `Instant` (for UTC timestamps), `LocalDate`, `LocalDateTime`, `ZonedDateTime`, `Duration`, and `Period`. Avoid using legacy `java.util.Date` and `java.util.Calendar` classes.
* **Logging Facade (SLF4J):** Always use the SLF4J API for logging. The concrete implementation (Logback) is configured via Spring Boot.

#### 13.1.5 Code Generation Anti-Patterns to Avoid (Especially for AI Agent Guidance)

* **Overly Complex Lambdas/Functional Chains:** While functional programming is encouraged, avoid excessively long or deeply nested lambda expressions or functional chains that become difficult to read and debug. Break them down into smaller, well-named functions if necessary.
* **Unjustified `lateinit` or `!!`:** Minimize the use of `lateinit` (prefer constructor injection or nullable types with proper initialization). Strictly avoid `!!` unless accompanied by a strong, commented justification (see Null Handling).
* **Ignoring Function Return Values:** Do not ignore return values from functions, especially if they indicate success/failure, or if they are pure functions returning a new state (common with immutable objects). For Axon, `CommandGateway.send(command).join()` or similar constructs that might block or throw exceptions need careful handling of their results or exceptions.
* **Generic Exception Catching (`catch (e: Exception)` or `catch (e: Throwable)`):** Avoid catching overly broad exceptions unless it's at a top-level error boundary and the exception is appropriately logged and re-thrown as a more specific or wrapped exception, or handled definitively. Catch specific exceptions where possible.
* **Blocking Calls in Non-Blocking Contexts:** Do not make blocking I/O calls or use `runBlocking {}` within Kotlin coroutines running on default or UI dispatchers, or within reactive streams, without explicitly shifting to a blocking-IO-appropriate dispatcher (e.g., `withContext(Dispatchers.IO)`).
* **Mutable State in Spring Components:** Strive for stateless Spring components (`@Service`, `@RestController`, `@Component`) where possible. If state is necessary, ensure it is managed correctly, especially concerning concurrency (e.g., use thread-safe constructs or appropriate scoping).
* **Hardcoding Configuration:** Avoid hardcoding configuration values (URLs, credentials, timeouts, feature flags). Use Spring Boot's externalized configuration mechanisms (`application.yml`, environment variables, `@ConfigurationProperties`).
* **Directly Using `entityManager` or `JdbcTemplate` in Command Side of CQRS:** In a CQRS system with Axon, the command side (Aggregates) should not directly interact with JPA EntityManagers or JdbcTemplates to modify state that is also managed by event sourcing or affects read models directly. State changes go through events; read models are updated by event handlers. Direct DB access on the command side is permissible for looking up data needed for validation if that data is not part of the aggregate's state and not managed via Axon queries.

## 14. Overall Testing Strategy

This section outlines the project's comprehensive testing strategy, which all AI-generated and human-written code must adhere to. It complements the testing tools selected in the "Definitive Tech Stack Selections" and the NFRs regarding test coverage. A multi-layered testing approach will be adopted to ensure software quality.

* **Primary Testing Tools & Frameworks:**
  * **Backend (Kotlin/JVM):**
    * **Unit & Integration Tests:** JUnit Jupiter (`5.12.2`), MockK (`1.14.2`) for mocking, Kotest (`5.9.1`) or AssertJ for assertions.
    * **Axon Specific Tests:** Axon Test Fixture (version aligned with Axon Framework `4.11.2`).
    * **Integration Test Dependencies:** Testcontainers (`1.21.0` for Java) to manage Dockerized dependencies like PostgreSQL.
    * **Code Coverage:** JaCoCo Gradle plugin.
  * **Frontend (Control Plane UI - React):**
    * **Unit & Component Tests:** Jest (`29.7.0`), React Testing Library (`16.3.x`).
    * **End-to-End (E2E) Tests:** Playwright (`1.52.x`).
  * **CI Integration:** All automated tests (Unit, Integration, relevant E2E) will be executed as part of the CI/CD pipeline (GitHub Actions) for every pull request and main branch commit. Build failures will occur if tests fail or if coverage drops below defined thresholds.

* **Unit Tests:**
  * **Scope:** Test individual functions, methods, classes, or small, isolated modules (e.g., a single Spring service, a domain entity's logic, a utility function, an Axon Aggregate's command/event handlers in isolation). The focus is on verifying business logic, algorithms, transformation rules, and boundary conditions in isolation from external dependencies.
  * **Location & Naming (Kotlin - Backend):**
    * As defined in "Coding Standards": Unit test files must be located in the `src/test/kotlin/` directory of their respective module. The package structure within `src/test/kotlin/` must mirror the package structure of the code being tested.
    * Test class files must be named after the class they are testing, appended with `Test` (e.g., `TenantServiceTest.kt`).
  * **Mocking/Stubbing (Backend):**
    * **MockK** is the preferred library for creating mocks, stubs, and spies in Kotlin unit tests.
    * All external dependencies (e.g., other services, database repositories, network clients, file system interactions, system time if relevant) must be mocked or stubbed to ensure tests are isolated and run quickly.
    * **Axon Test Fixture:** Use for testing Axon Aggregates by providing a given-when-then style of test for command handlers and event sourcing logic.
  * **AI Agent Responsibility:** AI Agents tasked with code generation or modification must generate comprehensive unit tests covering all public methods of new/modified classes, significant logic paths (including happy paths and edge cases), and error conditions.

* **Integration Tests (Backend):**
  * **Scope:** Test the interaction and collaboration between several components or services within the application's boundary, or between the application and external infrastructure it directly controls (like a database). Examples:
    * API endpoint through to the service layer and (test) database.
    * Interaction between an Axon Command Handler, Event Store, and an Event Handler updating a read model.
    * Communication between different internal EAF modules if they have direct synchronous or asynchronous interfaces (beyond core event bus interactions for CQRS).
  * **Location & Naming (Backend):**
    * Typically reside in `src/test/kotlin/` alongside unit tests but may be distinguished by:
      * A specific naming convention (e.g., `*IntegrationTest.kt`).
      * Being placed in a dedicated package (e.g., `com.axians.accieaf.[module].integration`).
    * Alternatively, a separate Gradle source set (e.g., `src/integrationTest/kotlin`) can be configured if a stronger separation is desired.
  * **Environment & Dependencies:**
    * **Testcontainers:** Use Testcontainers to manage lifecycles of external dependencies like PostgreSQL instances for integration tests. This ensures tests run against a real database engine in a clean, isolated environment.
    * Spring Boot's testing support (`@SpringBootTest`) will be used to load application contexts for testing service interactions.
  * **AI Agent Responsibility:** AI Agents may be tasked with generating integration tests for key service interactions or API endpoints based on defined specifications, particularly where component collaboration is critical.

* **End-to-End (E2E) Tests (Primarily for Control Plane UI):**
  * **Scope:** Validate complete user flows or critical paths through the system from the end-user's perspective. For the ACCI EAF, this primarily applies to the Control Plane UI and its interaction with the `eaf-controlplane-api`.
  * **Tools:** **Playwright (`1.52.x`)** will be used for E2E testing of the Control Plane UI.
  * **Test Scenarios:** Based on user stories and acceptance criteria for the Control Plane UI features (e.g., logging in, creating a tenant, assigning a license, configuring an IdP).
  * **Execution:** E2E tests are more resource-intensive and will typically run less frequently than unit/integration tests (e.g., nightly builds, pre-release pipelines) but critical smoke tests might run on every PR.
  * **AI Agent Responsibility:** AI Agents may be tasked with generating E2E test stubs or scripts (e.g., Playwright page object models, basic test scenarios) based on user stories or UI specifications.

* **Test Coverage:**
  * **Target (as per PRD NFR 4a):**
    * Core EAF modules (`eaf-*`) aim for **100% unit test coverage for critical business logic**.
    * For new business logic developed within the EAF or applications based on it, a high unit test coverage of **>80% (line and branch)** is targeted.
  * **Measurement:** **JaCoCo** Gradle plugin will be used to measure and report code coverage for Kotlin/JVM code. Coverage reports will be generated as part of the CI build.
  * **Quality over Quantity:** While coverage targets are important, the quality, relevance, and effectiveness of tests are paramount. Tests must be meaningful and verify actual behavior and requirements.

* **Mocking/Stubbing Strategy (General):**
  * **Clarity and Maintainability:** Prefer test doubles (fakes, stubs) that improve the clarity and maintainability of tests over overly complex mocking setups with extensive behavior verification if the latter makes tests brittle or hard to understand.
  * **Focus:** Mocks are primarily for isolating the unit under test and verifying interactions with its direct collaborators. Stubs provide controlled inputs. Fakes provide lightweight implementations of dependencies.
  * **Speed and Reliability:** Strive for tests that are fast, reliable (no flakiness), and isolated from each other.

* **Test Data Management:**
  * **Unit Tests:** Test data should be created directly within the test methods or setup methods (`@BeforeEach`) and be specific to the test case. Use helper functions or builder patterns for creating complex test objects.
  * **Integration/E2E Tests:**
    * **Fixtures/Factories:** Develop test data factories or fixture loading mechanisms to create consistent and reusable test data sets.
    * **Database Seeding:** For integration tests involving databases, use mechanisms like SQL scripts run via Testcontainers, or application-level data seeding before test execution.
    * **Isolation:** Ensure test data is isolated between test runs and tests (e.g., by cleaning and re-initializing the database or test containers before each test class or method). Spring Boot's `@DirtiesContext` can be used where appropriate if application context state is modified.
    * **Liquibase for Test Schemas:** Liquibase can also be used to manage schemas for test databases used in integration tests.

## 15. Security Best Practices

Security is a paramount concern for the ACCI EAF and applications built upon it. The following best practices are mandatory and must be actively addressed by all developers (human and AI agents) throughout the development lifecycle. These practices aim to mitigate common vulnerabilities (including those listed in OWASP Top 10) and ensure compliance with relevant NFRs.

* **Input Sanitization and Validation (OWASP A03:2021-Injection):**
  * **API Input Validation:** All data received from external sources (API request payloads, query parameters, headers) by any EAF API (e.g., `eaf-controlplane-api`, `eaf-license-server`) must be rigorously validated at the boundary before processing.
    * Utilize Spring Validation API (Bean Validation with JSR 303/380 annotations like `@Valid`, `@NotNull`, `@NotEmpty`, `@Size`, `@Pattern`, custom validators) on DTOs in `@RestController` methods.
    * Validation should cover data types, formats, lengths, ranges, and allowed character sets.
  * **Contextual Escaping/Encoding for Other Inputs:** Data from less direct sources (e.g., configuration files, database, messages from other internal systems) that might be used in potentially risky operations (like constructing log messages, queries, or file paths) should be handled with care, using appropriate escaping or encoding if they are ever reflected in outputs or used in sensitive sinks.
  * **Preventing Log Injection:** When logging user-supplied or external data, ensure it is properly handled by the structured logging framework to prevent log forging or injection of malicious characters (e.g., CRLF injection). Parameterized logging helps significantly.

* **Output Encoding (OWASP A03:2021-Injection, specifically XSS):**
  * **JSON APIs:** For REST APIs like `eaf-controlplane-api` and `eaf-license-server` that return JSON, Spring Boot with Jackson automatically handles proper JSON encoding, which mitigates XSS vulnerabilities within a JSON context. Ensure content types are correctly set to `application/json`.
  * **Control Plane UI (React):** The React-based Control Plane UI is responsible for its own XSS prevention. This includes:
    * Leveraging React's default JSX string encoding.
    * Avoiding direct use of `dangerouslySetInnerHTML` with un-sanitized user-supplied content.
    * Using appropriate, vetted libraries for rendering complex content like Markdown if it can originate from users.
  * **Data to UI:** While the backend API provides JSON, any data originating from user input that is sent back to a UI should still be treated by the UI as potentially untrusted and handled with appropriate output encoding or sanitization mechanisms within the UI framework.

* **Secrets Management:**
  * **No Hardcoded Secrets:** Secrets (passwords, API keys, client secrets, encryption keys, database credentials) must **never** be hardcoded in source code, committed to version control, or included in build artifacts.
  * **Externalized Configuration:** Secrets must be managed externally and supplied to the application at runtime via Spring Boot's externalized configuration mechanisms. For Docker Compose deployments on customer VMs, this typically means:
    * Using an `.env` file (kept outside version control and secured on the VM) that Docker Compose injects as environment variables into the containers.
    * Mounting secret files directly into containers (e.g., via Docker Compose `secrets` or volume mounts) and configuring Spring Boot to read them.
  * **Logging:** Ensure secrets are never logged. Configure log masking for known secret patterns if necessary, though avoiding logging them in the first place is paramount.
  * **`eaf-iam` Client Secrets:** Client secrets for service accounts generated by `eaf-iam` must be handled securely, returned only upon creation, and stored hashed within the EAF.

* **Dependency Security & Software Bill of Materials (SBOM):**
  * **Vulnerability Scanning:** Implement automated dependency vulnerability scanning using tools like the **OWASP Dependency-Check Gradle plugin**. This scan must be integrated into the CI/CD pipeline and configured to fail the build if critical or high-severity vulnerabilities are detected in project dependencies or their transitive dependencies.
  * **Regular Updates:** Regularly review and update dependencies to their latest secure versions, following the policy defined in "Dependency Management".
  * **SBOM Generation & Review (PRD NFR 9):**
    * Automatically generate an SBOM (Software Bill of Materials) in a standard format (e.g., CycloneDX, SPDX) for every EAF release and for applications built upon it. This will be part of the CI/CD pipeline.
    * Establish a process for continuously reviewing these SBOMs for license compliance and newly discovered vulnerabilities in third-party components (e.g., using OWASP Dependency Track or similar tools).

* **Authentication and Authorization Checks (via `eaf-iam`):**
  * **Enforce Authentication:** All API endpoints (except potentially a few explicitly public ones like health checks, if any) must enforce robust authentication using mechanisms provided or integrated by `eaf-iam` (e.g., token-based for API clients, OIDC/SAML for users via Control Plane UI). This will be integrated via Spring Security.
  * **Enforce Authorization:** Authorization (permission/role-based access control - RBAC) based on definitions within `eaf-iam` must be enforced for all protected resources and operations. This should occur at the service layer or API entry points, utilizing Spring Security's method security (`@PreAuthorize`, `@PostAuthorize`) or fine-grained checks within business logic where appropriate.

* **Principle of Least Privilege:**
  * **Database Users:** The PostgreSQL user account(s) utilized by the ACCI EAF applications (e.g., `eaf-controlplane-api`, `eaf-license-server`, Axon Event Store user) must be granted only the minimum necessary DML/DDL permissions (SELECT, INSERT, UPDATE, DELETE on specific tables/schemas) required for their operation. Avoid using PostgreSQL superuser accounts for application runtime.
  * **OS Service Accounts (Docker Context):** Docker containers should be configured to run with non-root users where possible. Define specific users within Dockerfiles.
  * **Application Permissions:** Within the application logic, components should only have access to the data and operations necessary for their specific function.

* **API Security (General):**
  * **HTTPS Exclusively:** All external API communication (to and from `eaf-controlplane-api`, `eaf-license-server`) must be over HTTPS. SSL/TLS termination can be handled by a reverse proxy (e.g., Nginx/Traefik container within the Docker Compose stack) or configured directly in Spring Boot (less common for edge traffic but possible).
  * **Rate Limiting & Throttling:** Implement rate limiting on all public-facing API endpoints to protect against Denial-of-Service (DoS) attacks and API abuse. This can be implemented using libraries like Resilience4j (`RateLimiter`) within Spring Boot applications or via a reverse proxy.
  * **HTTP Security Headers:** Configure appropriate HTTP security headers to be returned by API responses to enhance browser security for the Control Plane UI. These include:
    * `Strict-Transport-Security (HSTS)`
    * `X-Content-Type-Options: nosniff`
    * `X-Frame-Options: DENY` (or `SAMEORIGIN`)
    * `Content-Security-Policy (CSP)` (can be complex but highly effective)
    * `X-XSS-Protection` (though largely superseded by CSP)
        These can be configured in Spring Security or the reverse proxy.
  * **Input Validation:** (Reiterated) Crucial for preventing injection attacks and ensuring data integrity.

* **Error Handling & Information Disclosure:**
  * As defined in the "Error Handling Strategy": Ensure that error messages returned to API clients or displayed in UIs do **not** leak sensitive internal system information (e.g., stack traces, detailed SQL error messages, internal file paths, library versions).
  * Log detailed technical errors server-side for diagnostics. Provide generic, user-friendly error messages or correlation IDs to the client.

* **Regular Security Audits & Testing (PRD NFR 2f):**
  * **Internal Code Reviews:** Security considerations must be a part of regular code reviews.
  * **Penetration Testing:** Plan for and conduct external penetration tests on the EAF and critical EAF-based applications before significant production deployments or major releases.
  * **SAST/DAST:** Consider integrating Static Application Security Testing (SAST) tools into the CI/CD pipeline. Dynamic Application Security Testing (DAST) can be performed on running applications in test environments.

* **Other Relevant Security Practices:**
  * **Data Encryption:**
    * **In Transit:** HTTPS is mandatory for external communication. Communication between containers within the Docker Compose stack on the same host (e.g., API to Database) may use the Docker network; if this network is not considered fully trusted or if compliance requires it, inter-service TLS should be considered.
    * **At Rest:**
      * Sensitive configuration data (e.g., secrets in `.env` files on the VM) must be protected by appropriate VM host security and file permissions (customer responsibility).
      * For data stored in PostgreSQL (e.g., hashed passwords in `eaf-iam`, sensitive audit log entries), consider options like PostgreSQL's Transparent Data Encryption (TDE) via extensions (e.g., `pgcrypto` for column-level encryption if needed, though this adds complexity) or rely on filesystem-level encryption on the VM host (customer responsibility).
    * **FIPS 140-2/3 Compliance (PRD NFR 2c):** All cryptographic operations (password hashing, JWT signing/validation, TLS configuration, etc.) must be performed using libraries and JVM configurations that support or utilize FIPS 140-2/3 validated cryptographic modules. This involves careful selection and configuration of the JVM (e.g., IBM Semeru Runtimes in FIPS mode, or OpenJDK with a FIPS-certified provider like BouncyCastle-FIPS) and cryptographic libraries.
  * **Session Management (Control Plane UI):**
    * If traditional session cookies are used by the UI's authentication mechanism (though token-based via API is more likely with React), ensure they are configured securely: `HttpOnly`, `Secure` (if UI is HTTPS), `SameSite` attributes.
    * For token-based authentication (e.g., JWTs issued by `eaf-iam` and consumed by the UI), use short-lived access tokens and secure mechanisms for storing and refreshing tokens if refresh tokens are used.
  * **Audit Logging (`eaf-observability`):**
    * Ensure comprehensive and immutable audit logs capture all security-relevant events: successful/failed logins, logouts, significant administrative actions (tenant creation, user modification, role changes, license assignments), access to sensitive data, changes to security configurations.
    * Protect audit logs from unauthorized access and tampering. Ensure they include sufficient detail (timestamp, user, action, outcome, relevant resource IDs).

## 16. Key Reference Documents

This section lists key documents that are either inputs to this architecture, provide further detail on specific aspects, or will be produced as complementary artifacts.

1. **ACCI EAF Product Requirements Document (PRD):**
    * *Location:* `docs/ACCI-EAF-PRD.md` (or the location provided by Product Management)
    * *Description:* The primary document detailing the functional and non-functional requirements, goals, context, user stories (Epics), and core technical decisions for the ACCI EAF. This Architecture Document is fundamentally based on the PRD.

2. **Official Kotlin Coding Conventions:**
    * *Location:* [https://kotlinlang.org/docs/coding-conventions.html](https://kotlinlang.org/docs/coding-conventions.html)
    * *Description:* The official style guide for Kotlin development, which forms the basis of the "ACCI Kotlin Coding Standards v1.0" detailed in this document.

3. **ACCI EAF API Specifications (OpenAPI):**
    * *Location (Planned):*
        * `docs/api/controlplane-v1.yml` (or `.json`)
        * `docs/api/licenseserver-v1.yml` (or `.json`)
    * *Description:* Detailed OpenAPI 3.x specifications for the RESTful APIs provided by `eaf-controlplane-api` and `eaf-license-server`. These will be generated/maintained during development.

4. **Architectural Decision Records (ADRs):**
    * *Location (Planned):* `docs/adr/`
    * *Description:* A collection of records documenting significant architectural decisions, their context, trade-offs, and consequences. *(The project structure has been extended by this directory.)*

5. **Frontend Architecture Document (Control Plane UI - if created separately):**
    * *Location (Hypothetical):* `docs/ACCI-EAF-Frontend-Architecture.md`
    * *Description:* If a separate detailed architecture document for the React-based Control Plane UI is created by the Design Architect, it would be referenced here. This document would elaborate on component structure, detailed state management, specific UI library usage patterns, etc., based on the guidelines from this main architecture document and the "Prompt for Design Architect" (see below).

## 17. Change Log

| Change                                     | Date             | Version | Description                                  | Author          |
| :----------------------------------------- | :--------------- | :------ | :------------------------------------------- | :-------------- |
| Initial Draft of Architecture Document     | 16. Mai 2025     | 0.1.0   | First complete draft based on PRD and inputs | Architect Agent |
|                                            |                  |         |                                              |                 |

*(This log will be updated as the architecture evolves.)*
