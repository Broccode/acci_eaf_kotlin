# 10. Definitive Tech Stack Selections
>
> This document is a granulated shard from the main "ACCI-EAF-Architecture.md" focusing on "Definitive Tech Stack Selections".

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
| **Languages**        | Kotlin                  | 2.1.21                                 | Primary language for backend EAF modules and applications   | Modern, concise, null-safe, excellent Java interoperability, good IDE support. Specified by PRD. |
| **Runtime**          | JVM (IBM Semeru Runtimes for Power or OpenJDK) | Java 21 (LTS)                          | Execution environment for Kotlin/Spring Boot applications on ppc64le | Standard für Kotlin/Java. IBM Semeru bietet optimierte Builds für Power-Architektur. JDK 21 als LTS.     |
| **Frameworks**       | Spring Boot             | 3.4.5                                  | Core application framework for backend modules and services     | Umfassend, etabliert, unterstützt schnelle Entwicklung, gute Integrationen. Von PRD vorgegeben. |
|                      | Axon Framework          | 4.11.2 \\<br/\\>(Upgrade auf v5 geplant)     | Framework for DDD, CQRS, Event Sourcing                     | Spezialisiert auf die gewählten Architekturmuster, gute Integration mit Spring. Von PRD vorgegeben. |
|                      | React                   | 19.1                                   | JavaScript library for building the Control Plane UI          | Populär, komponentenbasiert, großes Ökosystem. Von PRD vorgegeben.                          |
| **Databases**        | PostgreSQL              | 17.5                                   | Primary RDBMS for Event Store, Read Models, and State Data | Leistungsstark, Open Source, ACID-konform, gute Unterstützung für JSON. Von PRD vorgegeben.    |
| **Build Tool**       | Gradle                  | 8.14                                   | Build automation for the monorepo                           | Flexibel, gut für Kotlin & Multi-Projekt-Builds, Dependency Management. Von PRD vorgegeben. |
| **Infrastructure**   | Docker                  | Neueste stabile Engine-Version         | Containerization for deployment and development consistency on ppc64le | Ermöglicht portable Umgebungen, vereinfacht Deployment. Erwähnt in PRD NFRs.                   |
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
| **CI/CD**            | GitHub Actions          | N/A (Service)                          | Automation for build, test, and deployment pipelines        | Integriert in GitHub, flexibel konfigurierbar. Gemäß Projektstruktur.                     |
| **Other Tools**      | Logback                 | (Version via Spring Boot)              | Logging framework for backend                               | Standard in Spring Boot.                                                                 |
|                      | Micrometer              | (Version via Spring Boot)              | Application metrics facade                                  | Ermöglicht Metrik-Export (z.B. an Prometheus). Erwähnt in PRD.                           |
|                      | springdoc-openapi       | `Version passend zu Spring Boot 3.4.x` | Generates OpenAPI 3 documentation from Spring Boot controllers | Automatisiert API-Dokumentationserstellung.                                              |
|                      | Liquibase               | 4.31.1                                 | Tool for managing database schema changes (Read Models, etc.) | Notwendig für versionierte DB-Migrationen.                                               |

</rewritten_file>
