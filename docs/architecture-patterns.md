# 5.1 Architectural / Design Patterns Adopted
>
> This document is a granulated shard from the main "ACCI-EAF-Architecture.md" focusing on "Architectural / Design Patterns Adopted".

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
