# Epic 10: EAF Observability & Developer Experience (DX) Enhancements
>
> This document is a granulated shard from the main "ACCI-EAF-PRD.md" focusing on "Epic 10: EAF Observability & Developer Experience (DX) Enhancements".

*Description:* Implements the `eaf-observability` module (logging, metrics, health checks). Enhances developer documentation, provides example usage (`app-example-module`), and potentially CLI tools (`eaf-cli`).
*Value:* Improves operational readiness and developer adoption of the EAF.

**Story 10.1: Standardized Logging Framework for EAF Applications**

* **As an** EAF Developer, **I want** the `eaf-observability` module to provide a standardized, configurable logging framework (e.g., SLF4J with Logback, structured logging), **so that** EAF-based applications have consistent, effective, and monitoring-system-optimized application logging.
* **Acceptance Criteria (ACs):**
    1. The `eaf-observability` module integrates SLF4J as the logging facade and Logback as the default implementation. Dependencies are centrally managed.
    2. Default Logback configurations (e.g., `logback-spring.xml`) are provided, enabling structured logging in JSON format to `stdout`. The JSON format includes at least timestamp (ISO 8601 with milliseconds and timezone), log level, thread name, logger name, message, and optionally markers and MDC parameters.
    3. Configuration options (e.g., via Spring Properties) allow EAF applications to adjust log levels for different logger hierarchies at runtime, add custom appenders, and modify the log format if necessary, without compromising core EAF logging functionality.
    4. Propagation and inclusion of correlation IDs (e.g., Trace ID, Span ID from a distributed tracing system, or a per-request generated ID) in every log message via MDC are supported and enabled by default to facilitate tracing of flows.
    5. Documentation and code examples detail how EAF application developers can use the logging service, instantiate their own loggers, log structured information, and use MDC for additional context. Best practices for performant logging are explained.
    6. Guidance on log rotation and archival is provided in the documentation for typical deployment scenarios (e.g., VM with log management agents), though the EAF itself does not perform rotation/archival.
    7. Guidelines and examples for masking or omitting sensitive data (e.g., passwords, personal data) in log messages are provided.

**Story 10.2: Metrics Collection & Export for EAF Applications**

* **As an** EAF Developer, **I want** the `eaf-observability` module to integrate a metrics library (e.g., Micrometer) and provide a way to export metrics in a common format (e.g., Prometheus), **so that** the performance, resource utilization, and health of EAF-based applications can be centrally monitored and analyzed.
* **Acceptance Criteria (ACs):**
    1. The `eaf-observability` module integrates Micrometer as the standard metrics facade.
    2. Default system metrics are enabled and configured by default (e.g., JVM metrics like heap/non-heap usage, GC activity, thread count; CPU usage; Spring Boot Actuator metrics like HTTP request statistics, DataSource usage; Axon-specific metrics if provided by Axon).
    3. An HTTP endpoint (e.g., `/actuator/prometheus` via Spring Boot Actuator) is provided by default to expose metrics in Prometheus format for scraping by a Prometheus system. The endpoint is optionally securable via configuration.
    4. EAF application developers can easily and standardly (e.g., via annotations or programmatic registration with Micrometer) define and register custom, application-specific metrics (Counters, Timers, Gauges).
    5. Documentation and code examples for using metrics, configuring the Prometheus endpoint, and creating custom metrics are comprehensive and understandable. Recommendations for key metrics that applications should expose are given.
    6. The performance impact of metrics collection is minimal and configurable (e.g., sampling rates for certain metrics).
    7. The ability to disable default metrics if needed to reduce overhead is provided.

**Story 10.3: Standardized Health Check Endpoints for EAF Applications**

* **As an** EAF Developer, **I want** the `eaf-observability` module to provide standardized health check endpoints (e.g., via Spring Boot Actuator), **so that** the operational status and functionality of EAF-based applications can be easily monitored by external systems, load balancers, or orchestration platforms.
* **Acceptance Criteria (ACs):**
    1. The `eaf-observability` module configures Spring Boot Actuator's main health endpoint (e.g., `/actuator/health`) as well as liveness (`/actuator/health/liveness`) and readiness (`/actuator/health/readiness`) probes by default.
    2. The health endpoint by default includes checks for basic application status (e.g., application context is `UP`), the state of critical internal components (e.g., database connectivity via DataSource Health Indicator, connection to Axon Event Store), and disk space.
    3. EAF application developers can easily and standardly implement and register their own custom `HealthIndicator` components that influence the overall status of the health endpoint.
    4. Configuration of the health endpoint (e.g., which details are shown, caching duration) is possible via application properties. The endpoint is optionally securable via configuration.
    5. Documentation and code examples detail the use of standard health checks, their configuration, and the implementation and integration of custom health indicators.
    6. Behavior on failing health checks (e.g., returning HTTP 503 Service Unavailable) is clearly defined.

**Story 10.4: Comprehensive EAF Developer Documentation Portal**

* **As a** Developer using the ACCI EAF, **I want** a comprehensive, well-structured, versioned, and easily navigable documentation portal, **so that** I can quickly understand EAF concepts, APIs, usage patterns, architectural decisions, and best practices to apply them efficiently.
* **Acceptance Criteria (ACs):**
    1. A documentation portal or website is set up (e.g., using a static site generator like MkDocs, Docusaurus, Antora, or a well-structured wiki like Confluence if preferred internally). The documentation is versioned, and versioned instances are accessible online.
    2. The documentation is logically structured and includes at least the following sections:
        * **Introduction:** Vision, goals, architecture overview, core EAF concepts (DDD, CQRS, ES with Axon in ACCI context), technology stack.
        * **Getting Started:** Development environment setup, creating a first project/module with the EAF, "Hello World" example.
        * **Detailed How-To Guides:** Practical explanations for each EAF module (`eaf-core`, `eaf-iam`, etc.) covering configuration, usage, and extension points.
        * **API References:** Generated KDoc/JavaDoc for all public EAF APIs, ideally linked or embedded directly in the portal.
        * **Tutorials:** Step-by-step instructions for common, more complex use cases (e.g., implementing a full CQRS flow, configuring an external auth provider).
        * **Best Practices & Design Patterns:** Recommendations for designing applications based on the EAF.
        * **Troubleshooting & FAQ:** Solutions for common problems and answers to frequently asked questions.
        * **Contribution Guide:** If internal developers are to contribute to the EAF (coding standards, PR process, etc.).
    3. Documentation is created and kept current in parallel with EAF development. Every new feature or API change is documented before the feature is considered "Done."
    4. The documentation is written clearly and precisely, uses consistent terminology (a glossary is maintained), and targets developers who may be new to Kotlin/Java or the specific frameworks (Spring Boot, Axon). Code examples are correct, runnable, and follow EAF conventions.
    5. The documentation portal offers good search functionality.
    6. A mechanism for developers to provide feedback on documentation or suggest improvements is established.

**Story 10.5: `app-example-module` - Demonstrating EAF Best Practices**

* **As a** Developer new to the ACCI EAF, **I want** a fully functional, yet understandable, example application module (`app-example-module`) built using the EAF, **so that** I can see best practices in action and use it as a starting point, learning resource, or reference for my own developments.
* **Acceptance Criteria (ACs):**
    1. The `app-example-module` is created as a standalone Gradle sub-project within the monorepo and kept up-to-date.
    2. It demonstrates the integration and usage of key EAF core modules: `eaf-core` (implementing a CQRS/ES-based domain model), `eaf-iam` (e.g., secured endpoints, programmatic permission checks), `eaf-multitenancy` (all operations occur within a tenant context), `eaf-i18n` (basic multilingual UI texts, if UI is present), `eaf-observability` (logging, metrics, health checks).
    3. The example includes a simple but non-trivial domain model (e.g., a small "To-Do Management" or "Mini Order System") with at least 1-2 Aggregates, several Commands, Events, and Queries.
    4. It clearly shows how Commands, Events, Aggregates, Projections (Read Models), and Query Handlers are structured and implemented using Axon Framework within the ACCI EAF context.
    5. It has a simple UI (e.g., a few web pages using Thymeleaf/Spring MVC or a simple React application consuming the example module's APIs) or at least clearly defined REST API endpoints to interact with its features and demonstrate functionality.
    6. The example project includes its own unit and integration tests, demonstrating how to test EAF-based applications (especially CQRS/ES components).
    7. The code of the example module is well-structured, commented, and follows established EAF coding conventions and best practices.
    8. The example module is extensively described in the EAF developer documentation (Story 10.4), including instructions on how to run and explore it.

**Story 10.6: Basic EAF Command-Line Interface (CLI) Tool (`eaf-cli`)**

* **As an** EAF Developer, **I want** a basic Command-Line Interface (CLI) tool provided by the EAF, **so that** I can perform common development, diagnostic, or administrative tasks related to the EAF and applications based on it more efficiently from the console.
* **Acceptance Criteria (ACs):**
    1. The `eaf-cli` module is created as a standalone Gradle sub-project and uses an established CLI framework for Java/Kotlin (e.g., PicoCLI, Spring Shell).
    2. At least 1-2 useful initial commands for the MVP are implemented, e.g.:
        * `eaf-cli project scaffold --name <module-name>`: Generates a basic structure for a new EAF application module based on a template.
        * `eaf-cli diagnostic check-env`: Checks basic environment prerequisites for EAF development (e.g., JDK version, Gradle availability).
        * `eaf-cli version`: Displays the version of the EAF and its core components.
    3. The CLI tool is packaged as an executable JAR or native image (e.g., with GraalVM, if feasible and sensible for CLI tools on ppc64le) for easy distribution and use by developers.
    4. The CLI tool is well-documented, including installation instructions and detailed descriptions of all commands and their options. Every command supports a `--help` parameter.
    5. The CLI provides clear, informative success and error messages. Errors are signaled with appropriate exit codes.
    6. The CLI handles any configuration files or required credentials securely (if applicable for certain commands).

**Story 10.7: Implement Dedicated Audit Logging Mechanism**

* **As an** EAF Developer, **I want** the EAF to provide a dedicated, persistent audit logging mechanism, separate from application-level logging or the event store, **so that** critical administrative, security, and license-related events can be securely recorded for compliance and analysis in a tamper-evident manner.
* **Acceptance Criteria (ACs):**
    1. A dedicated audit logging component or service is implemented within `eaf-core` or `eaf-observability`. This service is distinct from the standard application logging infrastructure (Story 10.1).
    2. Audit events are defined as structured objects with a clear, extensible schema. Minimum attributes for each audit event include: Timestamp (with milliseconds and timezone), Event ID (unique), Performing Actor (User ID, Service Account ID, System Process), Source IP Address (if applicable), Action/Event Type (e.g., `TENANT_CREATED`, `USER_LOGIN_FAILED`, `ROLE_ASSIGNED`, `LICENSE_ACTIVATED`), Target Resource (e.g., Tenant ID, User ID, Role Name), Outcome of the action (`SUCCESS`, `FAILURE`), and optionally, additional details (e.g., changed fields in an update, reason for failure).
    3. Audit logs are written to a secure, persistent store. For MVP, this can be a dedicated PostgreSQL table (`audit_log`) protected from unauthorized access and modification. The table is optimized for queries (e.g., indexes on timestamp, actor, action, target resource). Alternatively or additionally, output to a write-only log file format suitable for ingestion by external SIEM systems can be supported.
    4. The mechanism for generating and submitting audit events from relevant EAF modules (e.g., `eaf-iam` for login events, `eaf-multitenancy` for tenant lifecycle events, `eaf-licensing` for license activities, Control Plane backend for administrative actions) to the audit logging service is clearly defined and implemented (e.g., via an asynchronous event bus or direct service calls).
    5. Clear guidelines and a simple API are provided so that EAF-based applications can also write their own application-specific, but security-relevant, events to this central, EAF-managed audit log.
    6. The design considers aspects of audit log integrity and non-repudiation (e.g., no delete or update capabilities for existing entries via normal application logic; possibly hash-chaining or digital signatures for log batches as a future enhancement).
    7. Access to the audit log (for read purposes by authorized auditors or security personnel) is possible via defined interfaces or tools (e.g., specific API endpoints in the Control Plane, direct DB access with restricted permissions).
    8. Configuration of audit log storage (e.g., database connection, log level for audit events) and retention policies are documented (implementation of automated retention may be outside MVP scope).
    9. Audit logging must not significantly impair the performance of the core application (e.g., through asynchronous processing, batching).

*Note on integration with earlier Epics:* Story 2.4 (Basic Control Plane API for Tenant Management) will implement a placeholder/basic logging for tenant CUD operations. Full integration with the comprehensive audit logging mechanism defined in this Story 10.7 will be required once this story is implemented. Similar placeholder approaches may be used in other early stories requiring audit trails, with a full integration pass planned post-Epic 10 completion.
