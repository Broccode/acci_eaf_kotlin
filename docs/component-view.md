## 5. Component View
>
> This document is a granulated shard from the main "ACCI-EAF-Architecture.md" focusing on "Component View".

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
