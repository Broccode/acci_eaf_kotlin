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

The functional requirements for the MVP of the ACCI EAF are structured into 10 Epics. Each Epic is detailed with User Stories and their Acceptance Criteria, incorporating considerations for edge cases, error handling, and best practices.

**Epic 1: EAF Foundational Setup & Core Infrastructure**
*Description:* Establishes the initial ACCI EAF project structure within the Gradle monorepo, including `build-logic`, core configurations, CI/CD pipeline setup (initially for a simple target environment), and the basic `eaf-core` module with initial Axon Framework configuration. This Epic delivers a runnable, albeit functionally empty, framework foundation.
*Value:* Foundational for all further development, enables early CI/CD and target environment validation.

**Story 1.1: Monorepo Project Setup with Gradle**

* **As a** Developer, **I want** a new Gradle monorepo initialized for the ACCI EAF project, **so that** all EAF modules can be managed centrally and consistently.
* **Acceptance Criteria (ACs):**
    1. A Git repository for the ACCI EAF project is locally initialized, and a comprehensive `.gitignore` file (ignoring typical IDE, OS, build artifacts, and sensitive files) is present.
    2. A Gradle root project is set up with a `settings.gradle.kts` file declaring `build-logic` and the initial `eaf-core` module as sub-projects. The Gradle version is set to a current, stable version.
    3. A `build-logic` module is created with a basic structure for Gradle Convention Plugins or Version Catalogs to centrally and consistently manage shared build configurations (e.g., Java/Kotlin version, compiler options, standard plugins like Checkstyle/Klint) and dependency versions.
    4. The `eaf-core` module is created as a valid Gradle sub-project (Kotlin-based) and configured to potentially use configurations/dependencies from `build-logic`.
    5. The command `./gradlew build` (using the Gradle Wrapper) executes successfully without compilation errors or significant warnings indicating misconfiguration. All defined linting and style checks (if initially configured) pass. Expected build artifacts (e.g., empty JAR files, if applicable) are produced.
    6. The Gradle Wrapper files (`gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`, `gradle-wrapper.properties`) are correctly configured, present in the repository, and executable, ensuring consistent builds across different development environments. The wrapper is configured to use a specific Gradle version suitable for the project.
    7. A basic `README.md` file is present in the root directory, containing the project name, a brief description of the EAF, development environment prerequisites (JDK version, etc.), and basic instructions for cloning, building, and running the project.
    8. The setup is reproducible on a clean development environment (with the correctly installed JDK as per project requirements and without global Gradle installations that might interfere) and results in a successful build.

**Story 1.2: Basic `eaf-core` Module with Spring Boot & Axon Configuration**

* **As a** Developer, **I want** the `eaf-core` module to be a runnable Spring Boot application with initial Axon Framework configuration, **so that** the foundational CQRS/ES capabilities can be established and tested.
* **Acceptance Criteria (ACs):**
    1. The `eaf-core` module includes the necessary dependencies for Spring Boot (e.g., `spring-boot-starter-web` for web capabilities, `spring-boot-starter-actuator` for monitoring endpoints), managed via `build-logic`.
    2. The `eaf-core` module includes the core Axon Framework dependencies (e.g., `axon-spring-boot-starter`), managed via `build-logic`. The Axon version is set to a current, stable version.
    3. A basic Axon configuration is present: Command Bus, Query Bus, and Event Bus/Event Store (initially configured for in-memory operation for simple tests or a basic, local PostgreSQL connection for development tests) are correctly initialized as Spring Beans. Errors during the initialization of these Axon components (e.g., missing configuration, connection problems to the Event Store) lead to informative error messages at application startup and a failing health check.
    4. The `eaf-core` application can be started successfully without runtime errors using `./gradlew :eaf-core:bootRun` (or equivalent).
    5. The Spring Boot Actuator Health endpoint (`/actuator/health`) is available, reports an `UP` status, and includes basic health information for Axon components (e.g., Event Store connectivity, status of Event Processors if any are already defined).
    6. A simple test Command (e.g., `PingCommand`), a corresponding Command Handler (e.g., producing a `PongEvent`), and an Event Handler (e.g., logging the `PongEvent`) are implemented in the `eaf-core` module. Sending the command (e.g., via a test REST endpoint or an integration test) results in the expected event publication and processing. Errors in command processing (e.g., no handler found, validation error in handler) lead to a defined, traceable error response (e.g., standardized JSON error structure with error code and message) or a logged exception.
    7. The `application.properties` or `application.yml` file in the `eaf-core` module is structured (e.g., using Spring Profiles for `dev`, `test`, `prod`), contains placeholders or environment-specific configurations for Axon, database connection, etc., and is well-commented.
    8. A basic logging configuration (e.g., via Logback, configured in `eaf-observability` and used by `eaf-core`) is present and outputs informative, structured (e.g., JSON) log information during startup and execution of the test command/event, including timestamp, log level, thread name, and logger name.

**Story 1.3: Initial CI/CD Pipeline Setup**

* **As a** Development Team, **I want** a basic CI/CD pipeline configured for the ACCI EAF monorepo, **so that** code changes are automatically built, tested (initially unit tests and code quality checks), and a deployable artifact (e.g., Docker image for `eaf-core`) is produced.
* **Acceptance Criteria (ACs):**
    1. The CI pipeline (e.g., GitHub Actions Workflow, Jenkinsfile, GitLab CI YAML) is versioned as code within the monorepo and is automatically triggered on every push to the main branch (e.g., `main` or `develop`) and on every pull/merge request to this branch.
    2. The pipeline executes the command `./gradlew build cleanCheck` (or equivalent Gradle tasks) to compile all modules, perform static code analysis (linting, style checks via Checkstyle/Klint), and run unit tests. The pipeline fails if tests fail or configured quality gates (e.g., minimum test coverage, no critical code smells) are not met. Detailed test results and analysis reports are available as pipeline artifacts.
    3. The pipeline generates versioned build artifacts (e.g., JAR files for the modules) that include a manifest file with build information (e.g., Git commit ID, build timestamp, semantic version based on Git tags or build parameters).
    4. (Optional for initial setup, but recommended) The pipeline builds a Docker image for the `eaf-core` module using a multi-stage Dockerfile (optimized for minimal size, using a non-root user, correct layer structure). The image is tagged with the build version and published to an internal Docker registry (authentication to the registry is handled securely via secrets). A basic vulnerability scan of the Docker image (e.g., with Trivy) is performed, and critical findings lead to pipeline failure.
    5. The status of the pipeline (success/failure of each step) is clearly signaled in the respective CI/CD system and ideally also in the Git repository (e.g., as a commit status or pull request check). Notifications for failures are sent to the development team.
    6. Detailed and structured logs of the pipeline execution are accessible for at least 7 days to enable efficient troubleshooting.
    7. The pipeline is configured to use caching mechanisms (e.g., for Gradle dependencies, Docker layers) to optimize build times without compromising correctness.
    8. The pipeline handles errors in individual steps (e.g., temporary failure of the Docker registry) robustly (e.g., through retry mechanisms for certain operations) and reports them clearly.

---

**Epic 2: Core Multitenancy Implementation**
*Description:* Implements the foundational multitenancy capabilities within the `eaf-multitenancy` and `eaf-core` modules. This includes setting up RLS in PostgreSQL, mechanisms for tenant context propagation, and the basic API endpoints in the Control Plane backend for tenant CRUD operations.
*Value:* Enables tenant isolation, a core EAF feature. Allows basic tenant administration.

**Story 2.1: Tenant Entity & Basic Persistence**

* **As a** System Administrator (of the EAF), **I want** to define and persist tenant entities (e.g., tenant ID, name, status), **so that** tenants can be uniquely identified and managed within the system.
* **Acceptance Criteria (ACs):**
    1. A `Tenant` entity is defined within the `eaf-multitenancy` module (or `eaf-core`) with at least the attributes: `tenantId` (UUID type, primary key, system-generated upon creation and immutable), `name` (String, must be unique within the system, subject to validation rules for length and allowed characters, e.g., min 3 / max 100 chars, alphanumeric with hyphens), `status` (Enum, e.g., `ACTIVE`, `INACTIVE`, `SUSPENDED`, `PENDING_VERIFICATION`), `createdAt` (Timestamp, set on creation), `updatedAt` (Timestamp, updated on every modification).
    2. A PostgreSQL table (`tenants`) is created via an idempotent schema migration script (e.g., Flyway, Liquibase), including a tested rollback script. Necessary indexes (at least for `tenantId` (unique) and `name` (unique)) are present.
    3. Backend services for CRUD operations (Create, Read, Update, "Delete" as a soft-delete by changing status to `INACTIVE` or `ARCHIVED`) for `Tenant` entities are implemented. Actual physical deletion of tenants is not foreseen for MVP or is a highly restricted, logged administrative operation.
    4. The services validate input data for create and update operations (e.g., name format, status transitions) and handle database errors (e.g., unique constraint violation for `name`, connection problems) robustly by throwing informative, business-level exceptions or returning standardized error codes (which can be processed by the API layer).
    5. Comprehensive unit tests (e.g., with mocking of the database layer) and integration tests (e.g., with a test database) cover the CRUD operations for the `Tenant` entity, including all validation rules, success cases, and expected error cases (e.g., creating a tenant with an already existing name).
    6. The validation rules for tenant attributes (especially `name` and allowed `status` transitions) are clearly documented.

**Story 2.2: Tenant Context Propagation Mechanism**

* **As an** EAF Developer, **I want** a reliable mechanism for propagating the current tenant's context (e.g., tenant ID) throughout an application request, **so that** business logic and data access can be tenant-aware.
* **Acceptance Criteria (ACs):**
    1. A mechanism is implemented to securely capture and validate the `tenantId` at the beginning of a request (e.g., in an API Gateway, a preceding authentication filter, or directly in controllers). The expected source of the `tenantId` (e.g., from a validated JWT claim, a special HTTP header) is clearly defined and documented. If the `tenantId` is missing in a context where it is expected, or if it is invalid (e.g., format incorrect, tenant does not exist or is not active), a defined error (e.g., HTTP 400 Bad Request or HTTP 401 Unauthorized / HTTP 403 Forbidden) is returned before further business logic is executed.
    2. The validated `tenantId` is securely and immutably stored in a request-scoped context (e.g., using `ThreadLocal` for synchronous processing, `kotlinx.coroutines.ThreadContextElement` for coroutines, or as metadata in Axon messages). The context must not "leak" from one component to another or be incorrectly overwritten.
    3. Services within `eaf-core` and other EAF modules can reliably and easily access the current `tenantId` from this context via a clearly defined interface (e.g., `TenantContextHolder.getCurrentTenantId()`). Accessing the context when it is not set or incorrectly set (e.g., in a background process that should not be tenant-specific or mistakenly lacks context) leads to a defined behavior (e.g., error, use of a system default value if applicable and safe).
    4. The context propagation mechanism functions correctly and demonstrably in asynchronous operations, especially when using Kotlin Coroutines (`withContext`, `async`, `launch`), Spring's `@Async` methods, and within Axon Framework message flows (Commands, Events, Queries must transport the tenant context, typically in metadata).
    5. The mechanism is clearly documented for EAF application developers, including best practices for use and for implementing tenant-aware components. Explicit warnings about typical pitfalls (e.g., loss of context in new threads) are part of the documentation.
    6. Unit and integration tests verify the correct capture, storage, propagation, and retrieval of the tenant context in various scenarios: synchronous calls, asynchronous calls (coroutines, Axon handlers), valid context, missing context, invalid context (e.g., tenant not active).

**Story 2.3: Row-Level Security (RLS) Setup in PostgreSQL for Tenant Data**

* **As an** EAF Developer, **I want** RLS policies configured in PostgreSQL, **so that** database queries automatically filter data based on the current tenant context, ensuring strict data isolation.
* **Acceptance Criteria (ACs):**
    1. A clear and documented strategy for applying RLS to all tables that will contain tenant-specific data is defined. This strategy includes:
        * Every tenant-specific table *must* have a `tenant_id` column (UUID type), which cannot be null and has a foreign key to the `tenants.tenantId` column.
        * For which database user roles RLS is enabled by default (`FORCE ROW LEVEL SECURITY`) and for which it might be bypassed (e.g., `BYPASSRLS` attribute for highly privileged maintenance roles).
    2. Generic, activatable RLS policies (`CREATE POLICY`) are created in PostgreSQL via schema migration scripts. These policies use a database session variable (e.g., `current_setting('app.current_tenant_id', true)`) to filter data. There is at least one policy for `SELECT` and more restrictive policies for `INSERT`, `UPDATE`, `DELETE` ensuring data can only be modified in the correct tenant context.
    3. The EAF's database connection mechanism (e.g., DataSource wrapper in Spring/JPA or Axon JDBC Event Store configuration) is configured to correctly and securely set the database session variable `app.current_tenant_id` at the beginning of each transaction or request with the `tenantId` from the tenant context (from Story 2.2). If no tenant context is set, the variable is not set or is set to a value (e.g., `'-1'`) that guarantees no data access (except for explicitly tenant-agnostic system operations using a role with `BYPASSRLS`). Setting the variable must also be handled correctly with connection pooling (e.g., reset when returning the connection to the pool).
    4. At least two different example domain tables with tenant-specific data (with `tenant_id` column and foreign key) and applied RLS policies are created via schema migration for testing purposes.
    5. Comprehensive integration tests (operating with different database users and with/without set tenant contexts) rigorously demonstrate:
        * `SELECT` queries on the example datasets return *only* data for the `tenant_id` set in the session variable.
        * `INSERT` operations automatically set the correct `tenant_id` from the session variable or fail if an attempt is made to insert a different `tenant_id`.
        * `UPDATE`/`DELETE` operations only affect records of the current tenant.
        * Attempts to access or modify data of another tenant (even via "tricky" queries) fail or return empty results.
        * Operations without a set tenant context (or with an invalid one) result in no data access to tenant-specific tables (except for defined system roles/exceptions).
    6. The performance impact of RLS on common query types (including joins) is initially assessed (e.g., via `EXPLAIN ANALYZE`) and deemed acceptable for expected workloads. Necessary indexes to support RLS performance (especially on `tenant_id` columns) are present.
    7. The configuration and behavior of RLS are detailed for developers, including implications for database queries and schema design.

**Story 2.4: Basic Control Plane API for Tenant Management (CRUD)**

* **As a** Control Plane Administrator, **I want** a secure backend API to manage tenants (Create, Read, Update, Deactivate), **so that** I can administer tenant lifecycles.
* **Acceptance Criteria (ACs):**
    1. RESTful API endpoints are provided by the EAF (e.g., under `/api/controlplane/tenants`) and are located in `eaf-core` or a dedicated Control Plane backend module (e.g., `eaf-controlplane-api`). The API adheres to the principles of well-designed RESTful web services (correct use of HTTP methods, status codes, headers).
    2. The endpoints support HTTP methods for standard CRUD operations with clearly defined JSON request and response payloads:
        * `POST /tenants`: Creates a new tenant. Requires a valid tenant name and optionally other initial configuration parameters (e.g., initial admin email). Validates inputs server-side (e.g., name format, length, uniqueness of name, valid email). Returns HTTP 201 Created with the full tenant object (including system-generated `tenantId`) in the body and a `Location` header on success.
        * `GET /tenants`: Lists all tenants (or a paginated subset). Supports pagination (e.g., `page`, `size` query parameters with default values and upper limits) and filtering (e.g., by `status`, free-text search in `name`). Returns HTTP 200 OK with a list of tenant objects and pagination information in the body or headers. Returns an empty list if no tenants match the criteria.
        * `GET /tenants/{tenantId}`: Retrieves details of a specific tenant by its `tenantId` (UUID format). Returns HTTP 200 OK with the tenant object on success, or HTTP 404 Not Found if the tenant with the given ID does not exist.
        * `PUT /tenants/{tenantId}`: Updates an existing tenant (e.g., name, status). Is idempotent. Validates inputs. Returns HTTP 200 OK (with the updated object) or HTTP 204 No Content on success, HTTP 404 Not Found for a non-existent tenant, HTTP 400 Bad Request for validation errors.
        * `DELETE /tenants/{tenantId}`: Deactivates (soft delete by changing status to `INACTIVE` or `ARCHIVED`) a tenant. Is idempotent. Returns HTTP 204 No Content on success, HTTP 404 Not Found for a non-existent tenant.
    3. All API endpoints are secured by appropriate, robust authentication and authorization mechanisms (e.g., OAuth2 Client Credentials Flow for M2M access, specific admin roles). Unauthorized access attempts result in HTTP 401 Unauthorized; access attempts with insufficient permissions result in HTTP 403 Forbidden.
    4. API requests and responses consistently use JSON. Error responses follow a standardized format (e.g., RFC 7807 Problem Details for HTTP APIs) and include a machine-readable error code, a human-readable description, and optionally a trace identifier for correlation with server logs. Date formats in JSON are standardized (ISO 8601).
    5. All API operations that change the state of tenants (Create, Update, Delete/Deactivate) are implemented transactionally and atomically. In case of error, a rollback occurs to prevent inconsistent states.
    6. Current and detailed API documentation (e.g., generated from code with OpenAPI 3.x / Swagger) is available for these endpoints. It describes all endpoints, parameters, request/response schemas, validation rules, and possible error codes, and security requirements.
    7. Comprehensive integration tests (e.g., using Spring Boot Test, Testcontainers for the database) cover all API endpoints. Tested scenarios include happy paths, all defined validation errors, authorization and authentication errors, edge cases (e.g., operation on non-existent entities, empty lists, pagination limits), and idempotency of PUT/DELETE.
    8. All administrative changes to tenants (creation, update, status change) made via this API are recorded in detail in the central audit log (see Epic 10), including timestamp, performing actor, and changed data.

---

**Epic 3: Core Identity & Access Management (IAM) - Local Users & RBAC**
*Description:* Implements user management within a tenant (API in `eaf-iam`), local user authentication, and a foundational Role-Based Access Control (RBAC) system. Includes initial setup for service accounts.
*Value:* Allows secure user access and basic permission management within tenants.

**Story 3.1: Local User Entity & Secure Credential Storage (per Tenant)**

* **As an** EAF Developer, **I want** to define and securely persist local user entities (e.g., username, hashed password, status, associated tenant ID), **so that** tenant-specific users can be authenticated locally.
* **Acceptance Criteria (ACs):**
    1. A `LocalUser` entity is defined within the `eaf-iam` module with at least the attributes: `userId` (UUID, primary key, system-generated), `tenantId` (UUID, foreign key to `tenants.tenantId`, not null), `username` (String, must be unique per `tenantId`, subject to validation rules for length and allowed characters, e.g., email format or defined namespace), `hashedPassword` (String, stores the securely hashed password value), `salt` (String, if required by the hashing algorithm and not integrated into the hash value), `email` (String, optional, format validation), `status` (Enum: e.g., `ACTIVE`, `EMAIL_VERIFICATION_PENDING`, `LOCKED_BY_ADMIN`, `DISABLED_BY_ADMIN`, `PASSWORD_EXPIRED`), `createdAt` (Timestamp), `updatedAt` (Timestamp).
    2. A PostgreSQL table (`local_users`) is created via an idempotent schema migration script (including rollback). Necessary indexes (at least for `userId` (unique), (`tenantId`, `username`) (unique)) are present. The `hashedPassword` and `salt` columns are dimensioned to support modern hashing algorithms.
    3. A strong, adaptive hashing algorithm (e.g., Argon2id, scrypt, or bcrypt via Spring Security `DelegatingPasswordEncoder`) with appropriate configuration parameters (e.g., iteration count, memory cost, parallelism) is used for password storage. Plaintext passwords are never stored or logged at any time.
    4. Basic backend services (internal to the `eaf-iam` module) for creating (including password hashing), retrieving (excluding password hash), updating (excluding password), and searching for local users are implemented. These services validate input data and handle database errors robustly.
    5. Comprehensive unit tests cover user creation with correct password hashing, retrieval of users (without password data), and validation of user inputs (e.g., email format, uniqueness of `username` per tenant). Error cases (e.g., invalid inputs, database errors) are also tested.
    6. Password complexity policies (minimum length, character types) are definable (system configuration) and validated server-side during password creation/change.

**Story 3.2: API for Local User Management (within a Tenant)**

* **As a** Tenant Administrator (via Control Plane API), **I want** a secure backend API to manage local users within my tenant (Create, Read, Update, Set Status), **so that** I can control user access.
* **Acceptance Criteria (ACs):**
    1. RESTful API endpoints are provided by the `eaf-iam` module (e.g., under `/api/controlplane/tenants/{tenantId}/users`) and follow good API design principles.
    2. The endpoints support the following operations with clearly defined JSON Request/Response Payloads:
        * `POST /users`: Creates a new local user for the tenant specified in the path. Requires `username`, `password`, `email`. Validates inputs (password complexity, email format, uniqueness of `username`). Returns HTTP 201 Created with the user object (without password details).
        * `GET /users`: Lists local users of the tenant. Supports pagination, filtering (e.g., by `status`, `username`), and sorting. Returns HTTP 200 OK.
        * `GET /users/{userId}`: Retrieves details of a specific local user. Returns HTTP 200 OK or HTTP 404 Not Found.
        * `PUT /users/{userId}`: Updates details of a local user (e.g., `email`, `status`). `username` and `tenantId` are immutable. Validates inputs. Returns HTTP 200 OK or HTTP 404.
        * `POST /users/{userId}/set-password`: Allows an administrator to reset/change a user's password (may require admin confirmation). Validates new password against complexity rules. Returns HTTP 204 No Content.
        * `PUT /users/{userId}/status`: Updates user status (e.g., `ACTIVE`, `LOCKED_BY_ADMIN`, `DISABLED_BY_ADMIN`). Validates allowed status transitions. Returns HTTP 200 OK or HTTP 404.
    3. All API endpoints are secured by the tenant context (from the path `{tenantId}`) and appropriate permissions (e.g., "TenantUserAdmin" role). A tenant administrator may only manage users of their own tenant.
    4. Error responses follow the standardized format (RFC 7807 Problem Details). Validation errors list affected fields and specific issues.
    5. All operations that modify user data are transactional and recorded in the audit log.
    6. API documentation (OpenAPI 3.x) is detailed and current.
    7. Integration tests cover all endpoints, success cases, validation errors, authorization errors, and edge cases (e.g., attempting to edit a user of another tenant).

**Story 3.3: Local User Authentication Mechanism**

* **As a** User, **I want** to authenticate with my local EAF user credentials (username/password for a specific tenant), **so that** I can access tenant-specific applications/APIs.
* **Acceptance Criteria (ACs):**
    1. An authentication endpoint (e.g., `/api/auth/login` or `/oauth/token` if using OAuth2 Password Grant) is provided by the `eaf-iam` module or configured via Spring Security (possibly with customizations for tenant context).
    2. The endpoint accepts a tenant identifier (e.g., as part of the `username` in the format `user@tenantidentifier`, or as a separate parameter/header that is validated before authentication), the username (within the tenant), and the password.
    3. The authentication logic securely validates credentials against the stored hashed passwords and salts of local users (from Story 3.1). The user's `status` (`ACTIVE`) is also checked.
    4. Upon successful authentication, a secure, short-lived access token (e.g., JWT conforming to RFC 7519) is issued. The token contains at least `userId`, `tenantId`, `username`, and the user's assigned roles, as well as an `exp` claim (expiration time). A refresh token may optionally be issued for session extension.
    5. Failed authentication attempts (invalid username/password, locked/inactive account, invalid tenant) result in generic error messages (HTTP 400/401) without disclosing specific details about the failure reason (to prevent user enumeration). Repeated failed attempts for a user lead to a temporary account lockout (Account Lockout Policy, configurable). All authentication attempts (successful and failed) are securely logged (audit log and possibly security log).
    6. Integration tests verify successful and various failed authentication scenarios, token issuance and content, and account lockout behavior.
    7. The issued token mechanism (especially JWT) includes the `tenantId` in a way that it can be reliably extracted for subsequent tenant context propagation (Story 2.2).
    8. The security of the endpoint against brute-force attacks and other common authentication vulnerabilities is considered (e.g., through rate limiting, secure token handling).

**Story 3.4: RBAC - Role & Permission Definition and Assignment**

* **As an** EAF Developer, **I want** to define Roles and Permissions, and **as a** Tenant Administrator (via Control Plane API), **I want** to assign Roles to local users within my tenant, **so that** access to resources can be controlled based on roles.
* **Acceptance Criteria (ACs):**
    1. Entities for `Role` (e.g., `roleId`, `tenantId` (null for system-wide EAF admin roles, `tenantId` for tenant-specific roles), `name` (unique per `tenantId` or globally), `description`) and `Permission` (e.g., `permissionId`, `name` (unique, e.g., `user:create`, `tenant:edit`), `description`) are defined and persisted in the `eaf-iam` module. Permissions are initially system-defined and not creatable by tenant admins.
    2. A many-to-many relationship between `Role` and `Permission` is established and persisted via an intermediary table (roles can have multiple permissions; a permission can be in multiple roles).
    3. A many-to-many relationship between `LocalUser` and `Role` (within a tenant) is established and persisted via an intermediary table.
    4. Backend API endpoints (e.g., under `/api/controlplane/permissions` for global permissions, `/api/controlplane/roles` for global roles, `/api/controlplane/tenants/{tenantId}/roles` for tenant-specific roles, and `/api/controlplane/tenants/{tenantId}/users/{userId}/roles` for assignments) are provided for:
        * Listing all defined (system-wide) Permissions.
        * (For EAF Super-Admin) CRUD operations for system-wide Roles and assignment of Permissions to these Roles.
        * (For Tenant Admin) Listing available roles (applicable system-wide roles and own tenant-specific roles). Creating, Updating, Deleting tenant-specific roles. Assigning/revoking (from a pool of allowed) Permissions to/from tenant-specific roles.
        * Assigning/revoking Roles to/from users of the tenant.
    5. The authentication token (e.g., JWT from Story 3.3) includes the user's effective permission names (not just role names) or the role names if permission checking is done server-side against roles.
    6. The EAF provides a robust mechanism (e.g., integration with Spring Security Method Security using `@PreAuthorize` with custom expressions or via a central `AccessDecisionManager`) to protect services and API endpoints based on the assigned permissions (or roles) of the authenticated user (or service account). Lack of access results in HTTP 403 Forbidden.
    7. The basic RBAC setup, definition of permissions, and creation of roles are detailed for developers and administrators in the documentation.
    8. The design of RBAC structures and mechanisms considers future extensibility for ABAC concepts (e.g., permissions could potentially include contextual information or conditions, even if not fully utilized in MVP).
    9. All administrative changes to roles, permissions, and assignments are recorded in the audit log.

**Story 3.5: Service Account Management & Authentication with Default Expiration**

* **As a** Tenant Administrator (via Control Plane API), **I want** to create and manage Service Accounts for my tenant which have a default expiration, and **as an** External System, **I want** to authenticate using Service Account credentials, **so that** machine-to-machine API access can be secured and time-limited by default.
* **Acceptance Criteria (ACs):**
    1. A `ServiceAccount` entity is defined in the `eaf-iam` module with at least the attributes: `serviceAccountId` (UUID, PK), `tenantId` (UUID, FK), `clientId` (String, unique per tenant, system-generated), `clientSecretHash` (String, stores hash of the secret), `salt` (String), `description` (String), `status` (Enum: `ACTIVE`, `INACTIVE`), assigned `Role`-IDs, `createdAt` (Timestamp), `expiresAt` (Timestamp, nullable).
    2. When a Service Account is created via the API, `expiresAt` is set by default to a configurable value (e.g., 1 year from `createdAt`), unless a different expiration date (within a system-defined maximum allowed period) is explicitly provided during creation. A service account can also be created without an expiration date if explicitly specified (and administratively permitted).
    3. Secure generation, storage (hashing of the secret), and management (rotation of the secret, revocation) of client credentials are implemented. The client secret is displayed to the administrator only once immediately after creation or rotation and is not retrievably stored thereafter.
    4. Backend API endpoints (e.g., under `/api/controlplane/tenants/{tenantId}/service-accounts`) are provided for Tenant Administrators for CRUD management of Service Accounts. This includes creating, listing, viewing details (excluding secret hash), updating (description, status, `expiresAt`), and deleting (soft delete) Service Accounts, as well as triggering a secret rotation.
    5. A secure authentication mechanism for Service Accounts is implemented, preferably the OAuth 2.0 Client Credentials Grant Flow (`POST /oauth/token` with `grant_type=client_credentials`, `client_id`, `client_secret`). This mechanism must strictly check the `status` (`ACTIVE`) and `expiresAt` (must not be in the past) of the Service Account.
    6. Upon successful authentication of a Service Account, a short-lived access token (JWT) is issued, containing at least the `serviceAccountId`, `clientId`, `tenantId`, and the effective permissions/roles of the Service Account. The token's validity must not exceed the `expiresAt` time of the Service Account.
    7. Service Accounts can be assigned roles (similar to users) for RBAC to define their access permissions.
    8. The default expiration period (e.g., 1 year) and the maximum allowed expiration period for Service Accounts are configurable at the EAF system level.
    9. Failed authentication attempts by Service Accounts (invalid credentials, expired account, inactive account) are securely logged and result in a standardized error message (HTTP 400/401) without disclosing internal details.
    10. The API for managing Service Accounts validates all inputs (e.g., validity of `expiresAt`) and returns clear error messages for invalid data. All administrative changes to Service Accounts are recorded in the audit log.

---

**Epic 4: Control Plane UI - Phase 1 (Tenant & Basic User Management)**
*Description:* Develops the initial version of the Control Plane UI (React-Admin based) providing administrative capabilities for managing tenants (CRUD) and managing local users & their roles within those tenants (leveraging APIs from Epic 2 & 3).
*Value:* Provides a usable interface for core administrative tasks.

**Story 4.1: Control Plane UI Shell & Login**

* **As a** Control Plane Administrator, **I want** a basic UI shell (navigation, layout) for the Control Plane and a login screen, **so that** I can securely access the administrative functionalities.
* **Acceptance Criteria (ACs):**
    1. A new React application is initialized and configured for the Control Plane UI (e.g., using Create React App, Vite, or a similar established toolchain, with TypeScript as the standard language). The project includes basic linting and formatting rules.
    2. The UI uses an established component framework that approximates the style of React-Admin (e.g., Material-UI, Ant Design, or directly React-Admin components) to ensure a professional, functional, and consistent look and feel.
    3. A login page is implemented with input fields for username/email and password, and a login button. It securely calls the backend authentication API (from Story 3.3, possibly adapted for Control Plane Admins). CSRF protection is implemented if applicable.
    4. Upon successful login, an access token (e.g., JWT) is securely stored on the client (e.g., in `localStorage` or `sessionStorage` with XSS prevention considerations, or as an `HttpOnly` cookie if supported by the backend and suitable for the architecture). The user is redirected to a main dashboard or landing page. The "logged-in" state is persistent across page reloads (within token validity).
    5. Basic navigation (e.g., persistent sidebar, header with user menu and logout button) is present. Navigation displays only sections for which the logged-in administrator has permissions (RBAC-driven).
    6. The UI is primarily optimized for desktop browsers (current versions of Chrome, Firefox, Edge). Basic responsive display ensures core information is viewable on tablets without critical display errors or loss of functionality.
    7. Failed login attempts (e.g., invalid credentials, server error, locked account) are displayed to the user with a clear but non-detailed (to prevent enumeration) error message. Repeated failed attempts may trigger a short client-side delay before a new attempt is possible.
    8. A "password forgotten" flow is **not** part of this MVP phase; users are directed to a manual administrative process for password resets.
    9. The UI traps global JavaScript errors and unhandled API response errors (e.g., 5xx server errors), displaying a generic, user-friendly error message to prevent UI "freezing" or a blank white page. Client-side error logging (e.g., Sentry.io or simple `console.error` with potential backend logging) is considered.
    10. A logout button is present, invalidates the local session/token, and redirects the user to the login page.

**Story 4.2: UI for Tenant Management (CRUD)**

* **As a** Control Plane Administrator, **I want** a UI section to manage tenants (List, Create, View, Edit, Deactivate/Activate), **so that** I can perform tenant administration tasks visually.
* **Acceptance Criteria (ACs):**
    1. A "Tenant Management" section is securely accessible via the UI navigation (only for administrators with appropriate permissions).
    2. A data grid/table displays a list of tenants. Displayed columns include at least Tenant ID (possibly shortened/linked), Name, Status (e.g., Active, Inactive), and Creation Date. The table supports client-side or server-side pagination for large tenant lists, sorting by most columns, and free-text search/filtering (e.g., by name, status).
    3. A form (e.g., in a modal or separate page) allows creating new tenants (calling the API from Story 2.4). The form includes client-side validation for required fields (e.g., name) and data formats according to API specifications before sending the request. API error messages (e.g., "Name already exists," validation errors) are displayed understandably to the user, directly associated with the relevant fields or as a global form message.
    4. A read-only detail view (e.g., accessible by clicking a tenant in the list) allows displaying all relevant information of a selected tenant.
    5. A form allows editing existing tenants (e.g., name, status) (calling the API from Story 2.4). Client-side validation and contextual error handling for API errors are also implemented here. Fields that cannot be changed (e.g., `tenantId`) are not editable.
    6. Actions (e.g., buttons in the table row, context menu items, or buttons in the detail view) allow deactivating/activating tenants, preceded by a confirmation dialog (to prevent unintended actions). The tenant's status is correctly updated in the list after the action without requiring a manual refresh.
    7. User interactions are intuitive and follow the "React-Admin" style (e.g., clear buttons for primary and secondary actions, consistent form layouts, informative tooltips).
    8. Loading states (e.g., when fetching data for the table, saving changes in forms) are visually indicated to the user (e.g., via loading indicators, disabling buttons during action). Success and error messages after actions are clearly displayed (e.g., as "toast" notifications).

**Story 4.3: UI for Local User Management within a Tenant**

* **As a** Control Plane Administrator (or Tenant Administrator, if the UI later supports tenant-level admin logins and the logged-in user has appropriate rights for the selected tenant), **I want** a UI section to manage local users within a selected tenant (List, Create, View, Edit, Manage Status, Reset Password), **so that** tenant user administration can be done visually.
* **Acceptance Criteria (ACs):**
    1. Within a tenant's detail view or a dedicated "User Management" section (clearly scoped to a tenant and only accessible when a tenant is selected/in context), a data grid/table displays the local users of that tenant (columns: e.g., Username, Email, Status, Creation Date). Pagination, filtering, and sorting are supported.
    2. Forms and actions allow creating new local users (Username, Email, initial Password – password entered masked and only used for API submission), viewing user details, editing user information (e.g., Email, Status), and triggering password resets (calling APIs from Story 3.2). Validation, error handling, and loading states are implemented as described in Story 4.2.
    3. User status (Active, Locked, Disabled) can be managed via the UI, including confirmation dialogs for critical status changes.
    4. When creating a user or resetting a password, server-defined password strength policies (if any) are indicated client-side (e.g., as a tooltip) and validated server-side; corresponding API error messages are displayed.
    5. The UI never displays plaintext or hashed passwords at any time.
    6. Attempting to create a user whose username already exists within the tenant context results in a clear error message.
    7. User interactions are intuitive and consistent with the rest of the Control Plane.

**Story 4.4: UI for RBAC Management (Role Assignment within a Tenant)**

* **As a** Control Plane Administrator (or Tenant Administrator with appropriate rights), **I want** a UI section to manage role assignments to users within a selected tenant, **so that** access control can be configured visually.
* **Acceptance Criteria (ACs):**
    1. A UI section (e.g., in the user detail view of a local user or service account, or as a separate "Roles" tab/section) allows viewing the roles available for the current tenant (both system-wide applicable roles and tenant-specific ones, if supported later).
    2. For a selected user/service account, their currently assigned roles are clearly displayed.
    3. For a selected user/service account, roles can be assigned or unassigned from a list of available roles for the tenant (e.g., via a multi-select box, a list with checkboxes, drag-and-drop interface). Changes call the appropriate APIs from Story 3.4.
    4. An explicit save action with a confirmation dialog is required before changes to role assignments take effect.
    5. *(MVP Focus for Phase 1):* Creating/editing roles themselves and assigning permissions to roles is **not** done via the UI in this phase. The UI focuses solely on assigning *existing, predefined* roles to users/service accounts.
    6. User interactions are intuitive and error-tolerant (e.g., attempting to assign a non-existent role should be prevented by UI selection).
    7. Errors during saving of role assignments (e.g., API error, concurrent modification) are clearly communicated to the user. Loading states are displayed.

**Story 4.5: UI for Service Account Management within a Tenant**

* **As a** Control Plane Administrator (or Tenant Administrator with appropriate rights), **I want** a UI section to manage service accounts within a selected tenant (List, Create, View, Edit, Manage Status, Manage Credentials, View/Manage Expiration), **so that** M2M access can be administered visually.
* **Acceptance Criteria (ACs):**
    1. Within a tenant's detail view or a dedicated "Service Account Management" section (scoped to a tenant), a data grid/table displays the service accounts of that tenant (columns e.g., Client ID, Description, Status, Creation Date, Expiration Date). Pagination, filtering, and sorting are supported.
    2. Forms and actions allow creating new service accounts (with display of default expiration date and option for adjustment, if applicable), viewing details (including Client ID and expiration date, but *without* the client secret), editing information (e.g., description, status, expiration date), and managing credentials (e.g., option to regenerate client secret – the new secret is then displayed *once* and must be copied securely by the admin; Client ID is visible) (calling APIs from the updated Story 3.5).
    3. The status (Active, Disabled) and expiration date of service accounts can be managed via the UI.
    4. Visual indicators (e.g., color coding, icons) in the list and detail view highlight service accounts that are expiring soon or already expired.
    5. User interactions are intuitive. Copying the Client ID and the once-displayed Client Secret is facilitated by UI elements (e.g., "Copy" button).
    6. Clear warnings and confirmation dialogs are displayed before a service account is deleted/deactivated or a client secret is rotated/regenerated.
    7. Validation, error handling, and loading states are implemented as described in Story 4.2.

---

**Epic 5: Core Licensing Mechanism**
*Description:* Implements the `eaf-licensing` module with support for creating, validating time-limited licenses, and offline license activation/validation. Includes basic API endpoints in the Control Plane backend for internal license generation.
*Value:* Enables basic licensing capabilities for EAF-based products.

**Story 5.1: License Entity Definition & Secure Storage**

* **As an** EAF Developer, **I want** to define a `License` entity that supports time-limitation and secure storage mechanisms, **so that** licenses for EAF-based products can be represented and managed.
* **Acceptance Criteria (ACs):**
    1. A `License` entity is defined in the `eaf-licensing` module with at least the attributes: `licenseId` (UUID, PK, system-generated), `productId` (String, uniquely identifies the licensed product/module), `productVersion` (String, optional, for which product version the license is valid), `tenantId` (UUID, FK to `tenants.tenantId`, to whom the license is issued), `issueDate` (Timestamp), `validFrom` (Timestamp), `validUntil` (Timestamp, for time limitation), `licenseKey` (String, unique, securely generated and hard to guess/forge), `status` (Enum: e.g., `PENDING_GENERATION`, `ISSUED`, `ACTIVE`, `EXPIRED`, `REVOKED`, `INVALID`), `activationType` (Enum: e.g., `OFFLINE`, `ONLINE`), `maxCpuCores` (Integer, nullable, for later hardware binding from Epic 9), `features` (e.g., JSONB or text field storing a list of enabled feature flags or modules), `signedLicenseData` (String/BLOB, stores the cryptographically signed license information for offline validation).
    2. A PostgreSQL table (`licenses`) is created via an idempotent schema migration script (including rollback). Necessary indexes (at least for `licenseId` (unique), `licenseKey` (unique), (`tenantId`, `productId`)) are present.
    3. A robust mechanism for generating cryptographically secure, tamper-proof license keys and/or signed license files (e.g., using asymmetric cryptography like RSA or ECDSA) is implemented. The private key for signing is managed securely and is not part of the EAF code or the deployed application. The key management process (generation, storage, rotation of the private key) is documented.
    4. Basic backend services in the `eaf-licensing` module for creating (including signing), retrieving, and updating (e.g., status change) `License` entities are implemented. These services validate input data and handle database errors robustly.
    5. Unit tests cover the creation of licenses (including correct signing), retrieval, and validation of license attributes (e.g., `validFrom` must be before `validUntil`). Error cases (e.g., invalid inputs, signing errors) are also tested.
    6. The format of the `licenseKey` and `signedLicenseData` is clearly defined and versioned to allow for future changes.

**Story 5.2: Backend API for Internal License Generation (for ACCI Team)**

* **As an** ACCI Licensing Manager (via a Control Plane backend API), **I want** to generate new time-limited licenses for specific products and tenants, **so that** I can issue licenses to customers.
* **Acceptance Criteria (ACs):**
    1. Secure backend API endpoints are provided (e.g., under `/api/controlplane/licenses`, accessible only to highly authenticated and authorized ACCI personnel, e.g., via a dedicated admin role).
    2. The endpoints support the following operations with clearly defined JSON Request/Response Payloads:
        * `POST /licenses`: Creates a new license. Requires at least `productId`, `tenantId`, `validFrom`, `validUntil`, and optionally a list of `features`. The API internally generates the `licenseKey` and `signedLicenseData` (for offline activation). Returns HTTP 201 Created with the full license object (including `licenseKey` and `signedLicenseData` for one-time copy/download). Validates all inputs.
        * `GET /licenses`: Lists generated licenses. Supports pagination and comprehensive filtering (e.g., by `productId`, `tenantId`, `status`, validity period).
        * `GET /licenses/{licenseId}`: Retrieves details of a specific license (including all parameters except the private signing key).
        * `PUT /licenses/{licenseId}/status`: Allows updating the license status (e.g., to `REVOKED`). Validates allowed status transitions.
    3. All inputs to the API are validated server-side (e.g., valid dates, correct product IDs, `validFrom` before `validUntil`). Errors lead to HTTP 400 with detailed problem descriptions.
    4. API documentation (OpenAPI 3.x) is detailed and current, including a description of how `signedLicenseData` is provided to the customer.
    5. Integration tests cover all API functionalities, validation rules, authorization checks, and error cases.
    6. Every license generation and status change is recorded in detail in the central audit log (Epic 10).

**Story 5.3: Offline License Activation & Validation Mechanism for EAF Applications**

* **As a** Developer of an EAF-based application, **I want** the EAF to provide a mechanism to activate and validate a license offline (e.g., by importing a license file/string), **so that** my application can run in air-gapped environments.
* **Acceptance Criteria (ACs):**
    1. The `eaf-licensing` module provides a clear API or service (e.g., `LicenseActivationService.activateOffline(signedLicenseData)`) for an EAF-based application to submit the `signedLicenseData` (from Story 5.2).
    2. The submitted `signedLicenseData` is cryptographically validated client-side (within the EAF application) (signature check against the public key, integrity check of license data). The public key must be securely embedded within the EAF application.
    3. Upon successful validation, the license status (including `validFrom`, `validUntil`, `features`, `productId`, `tenantId`, and later hardware parameters from Epic 9) is securely stored locally on the application's system (e.g., in a protected file in the application's file system or a local configuration database). The storage location must be protected against simple user tampering as much as possible under the given OS constraints.
    4. The EAF application can query this locally activated license status at runtime via a defined interface in the `eaf-licensing` module.
    5. The process for generating the `signedLicenseData` (from Story 5.2), its secure delivery to the customer, and its import/activation in an EAF application (including error handling for faulty imports) is detailed in the documentation.
    6. Comprehensive test cases demonstrate successful offline activation and validation with valid licenses, as well as correct rejection of tampered, expired, or licenses issued for another product/tenant. Error messages on failed activation/validation are clear and diagnosable for the application developer.
    7. The mechanism is robust against simple attempts to tamper with the local license file or activation status (e.g., through checksums, internal consistency checks).

**Story 5.4: EAF Tooling for Application-Side License Checking**

* **As a** Developer of an EAF-based application, **I want** simple EAF-provided tools or an API to check the current license status (e.g., is active, expiry date, entitled features), **so that** I can easily implement license-aware behavior in my application.
* **Acceptance Criteria (ACs):**
    1. The `eaf-licensing` module exposes a clear, easy-to-use API (e.g., `LicenseService.isActive()`, `LicenseService.getLicenseDetails()`, `LicenseService.isFeatureEnabled("X")`, `LicenseService.getLicenseViolationReason()`).
    2. This service checks against the locally activated license status (from Story 5.3 for offline licenses or later from Story 9.5 for online licenses).
    3. The API is performant enough to be called potentially frequently (e.g., when accessing certain modules/features) without significantly impacting application performance (possibly through in-memory caching of the validated license status).
    4. The API is thread-safe.
    5. The API is well-documented with code examples showing how developers can use it to enable/disable features or display warning messages.
    6. The EAF provides basic information or enum-based return values that an application can use to react to specific license violations (e.g., license expired, feature not licensed, hardware binding violated). The specific implementation of the reaction (e.g., disable feature, terminate application, display warning) remains the responsibility of the EAF application.
    7. Unit tests cover the license checking API for various license states (active, expired, feature present/absent, etc.) and error cases (e.g., no license activated).
    8. The API returns clear and possibly localizable messages (or codes that the application can map to localized messages) that an application can display to the end-user in case of license problems.

---

**Epic 6: Internationalization (i18n) - Core Functionality & Control Plane Integration**
*Description:* Implements the `eaf-internationalization` module providing capabilities for loading translation files, language-dependent formatting, and language switching. Includes Control Plane UI features for tenants to manage their custom languages and translations.
*Value:* Enables multilingual applications and tenant-specific language customization.

**Story 6.1: EAF Core i18n Mechanism - Translation File Loading & Message Resolution**

* **As an** EAF Developer, **I want** the EAF to provide a robust mechanism for loading translation files (e.g., Java ResourceBundles) and resolving internationalized messages based on a user's locale, **so that** applications built on the EAF can easily support multiple languages.
* **Acceptance Criteria (ACs):**
    1. The `eaf-internationalization` module defines a clear strategy for organizing and loading translation files (e.g., `.properties` files in UTF-8 format, per language/locale, using standard Java `ResourceBundle` conventions). The strategy supports loading bundles from the application's classpath and potentially from external directories for later extensions.
    2. The EAF provides a central, easy-to-use service (e.g., a facade around Spring's `MessageSource`) that applications can use to retrieve localized messages by key and optional parameters.
    3. The mechanism supports parameterized messages (e.g., "Hello {0}, you have {1} new messages.") using the `java.text.MessageFormat` standard or an equivalent, secure method.
    4. A clearly defined fallback mechanism is implemented: If a translation for the requested locale and key does not exist, an attempt is made to fall back to a more general language (e.g., from `de_CH` to `de`) and finally to a configurable primary language of the EAF (e.g., English or German). If no key is found even there, a defined placeholder (e.g., `???key_name???`) or the key itself is returned, and a warning is logged.
    5. The setup and usage of the i18n mechanism (including file organization, key conventions, usage of the message service) are detailed for EAF application developers in the documentation.
    6. Comprehensive unit tests verify message resolution for various locales (including variants like `de_DE`, `de_CH`), correct fallback behavior for missing keys or languages, and correct processing of parameterized messages. The behavior with malformed resource bundle files (e.g., incorrect character encoding, syntax errors) is defined (e.g., error on startup, logged warning).
    7. The performance of message resolution is optimized (e.g., through caching of loaded ResourceBundles) to avoid significant overhead during frequent calls.

**Story 6.2: EAF Support for Locale-Specific Data Formatting**

* **As an** EAF Developer, **I want** the EAF to facilitate locale-specific formatting for numbers, dates, times, and currencies, **so that** data is presented correctly according to the user's language and cultural preferences.
* **Acceptance Criteria (ACs):**
    1. The `eaf-internationalization` module provides utility classes or clear guidance and examples for integrating and using standard Java/Kotlin libraries (e.g., `java.text.NumberFormat`, `java.text.DateFormat`, `java.time.format.DateTimeFormatter`, `java.util.Currency`) for locale-dependent formatting of numbers (decimal numbers, percentages).
    2. Corresponding utility classes/guidance are provided for locale-dependent formatting of dates and times (short, medium, long format). The use of `java.time` is recommended.
    3. Corresponding utility classes/guidance are provided for locale-dependent formatting of currency amounts (including currency symbol and correct positioning).
    4. Examples and documentation show EAF application developers how to securely use these formatting capabilities in conjunction with the user's current locale (determined via Story 6.3), both in backend logic (e.g., for generating reports) and in frontend components (possibly by providing locale info for client-side formatting).
    5. Error handling for invalid locale inputs to formatting functions is defined (e.g., fallback to default locale, exception).
    6. The documentation points out potential pitfalls in international formatting (e.g., different calendar systems, timezone issues – although in-depth timezone handling may go beyond pure formatting).

**Story 6.3: User Language Preference Management & Switching in EAF Applications**

* **As an** EAF Developer, **I want** the EAF to provide a simple way for applications to manage a user's language preference and allow users to switch languages, **so that** the application UI can be displayed in the user's chosen language.
* **Acceptance Criteria (ACs):**
    1. The EAF provides a mechanism to determine the current user's locale. The order of precedence is: 1. Explicit user selection (persistently stored), 2. Browser's `Accept-Language` header, 3. Configured default locale of the application/EAF.
    2. The EAF supports persisting the user's explicit language preference (e.g., as part of the user profile in the database (see `LocalUser` entity) or as a long-lived cookie/`localStorage` entry). The chosen method is secure and respects privacy.
    3. The EAF offers reusable components, server-side helper methods, or clear guidance for implementing a language switcher UI element (e.g., dropdown menu with available languages) in EAF-based web applications (especially for the Control Plane UI).
    4. Changing the language preference by the user results in the application interface (on next navigation or by dynamically reloading affected components) displaying texts and, if applicable, formatted data in the newly selected language, using the i18n mechanism from Story 6.1 and 6.2.
    5. The currently active locale is easily accessible server-side for the entire duration of a request (e.g., via a `LocaleContextHolder` or similar).
    6. If a user's persisted language preference points to an unsupported or invalid language, a defined fallback occurs (e.g., to the application's default language), and the user may be informed.

**Story 6.4: Control Plane API for Tenant-Specific Language Management**

* **As a** Tenant Administrator (via Control Plane API), **I want** to be able to add new custom languages for my tenant and manage their availability, **so that** my instance of an EAF-based application can support languages beyond the default set.
* **Acceptance Criteria (ACs):**
    1. Backend API endpoints (e.g., under `/api/controlplane/tenants/{tenantId}/languages`) are provided in the EAF and secured by appropriate permissions for tenant administrators.
    2. The endpoints support the following operations with JSON Payloads:
        * `POST /languages`: Adds a new custom language code (e.g., "fr-CA-custom", adhering to BCP 47 format or a defined convention) for the tenant specified in the path. Validates the language code for format and uniqueness per tenant.
        * `GET /languages`: Lists all languages available to the tenant (default application languages and custom languages added by the tenant), including their activation status.
        * `PUT /languages/{langCode}`: Updates properties of a custom language (e.g., display name, activation status `enabled/disabled`). Default application languages cannot be modified via this API (except possibly their activation/deactivation status for the tenant).
        * `DELETE /languages/{langCode}`: Removes a *custom* language for the tenant. Default application languages cannot be deleted. Confirmation is recommended. Deleting a language with existing translations leads to defined behavior (e.g., archival of translations or error if actively used).
    3. The EAF provides persistent storage (e.g., a dedicated DB table `tenant_languages`) for these tenant-specific language configurations, linked to the `tenantId`.
    4. The EAF's core i18n mechanism (Story 6.1) can recognize these tenant-defined languages and (if translations are present, see Story 6.5) consider them for message resolution for users of that tenant.
    5. All changes to a tenant's language settings are recorded in the audit log.
    6. Validation errors (e.g., invalid language code, attempt to delete a standard language) lead to clear HTTP 4xx error messages.

**Story 6.5: Control Plane API & UI for Tenant-Specific i18n Text Translation**

* **As a** Tenant Administrator (via Control Plane), **I want** to provide and manage my own translations for the application's i18n text keys for my custom languages (and potentially override default translations), **so that** I can fully localize the application for my users.
* **Acceptance Criteria (ACs):**
    1. Backend API endpoints (e.g., under `/api/controlplane/tenants/{tenantId}/languages/{langCode}/translations`) allow tenant administrators to submit and manage translations for all relevant i18n keys for their (tenant-activated) languages.
    2. The API supports at least:
        * `GET /`: Lists all i18n keys of the base application, ideally with the base language's default translations and the current tenant-specific translations for the specified `{langCode}`. Pagination and filtering by key or translation status (translated/untranslated/overridden) are supported.
        * `PUT /{messageKey}`: Creates or updates the tenant-specific translation for a given `{messageKey}` and the specified `{langCode}`. The request body contains the translation text. Validates inputs (e.g., for maximum length, preventing XSS via server-side sanitization if texts could be directly interpreted as HTML – however, it's better to let the frontend handle interpretation and store only plain text here).
        * `DELETE /{messageKey}`: Removes a tenant-specific translation for a key (causes fallback to the application's default translation).
    3. The EAF provides persistent storage (e.g., a dedicated DB table `tenant_translations` with `tenantId`, `langCode`, `messageKey`, `translationText`) for these tenant-specific translations.
    4. The EAF's core i18n message resolution mechanism (Story 6.1) prioritizes tenant-specific translations (from this storage) over default application translations when resolving a message for a given tenant and locale.
    5. A section in the Control Plane UI (React-Admin based, accessible to tenant administrators with appropriate permissions) is developed to:
        * List languages configured for the tenant (from Story 6.4).
        * Allow selection of a language for editing translations.
        * Display a paginated and filterable list of i18n keys, alongside the default translation (from the base application) and the input field/display for the tenant-specific translation.
        * Provide an intuitive interface (e.g., inline edit fields, save buttons per entry or for a group) for tenants to input, edit, and delete translations for each key in the selected language. Changes are saved via the API (see ACs 1-2).
        * Provide visual feedback on save status (saved, error, pending).
    6. The UI for translation management is robust and user-friendly, even with a large number of text keys (e.g., through efficient pagination, search/filter functions by key or content).
    7. Changes to tenant-specific translations are recorded in the audit log.
    8. The UI clearly indicates whether a translation is tenant-specific or from the application standard.

---

**Epic 7: Plugin System Foundation**
*Description:* Implements the `eaf-plugin-system` module (e.g., using ServiceLoader API) allowing for basic extensibility of the EAF by other modules. Includes a simple example plugin.
*Value:* Demonstrates and provides the core EAF extensibility.

**Story 7.1: Define EAF Extension Points using ServiceLoader Interfaces**

* **As an** EAF Developer, **I want** to define clear Java/Kotlin interfaces within `eaf-core` (or a dedicated `eaf-plugin-api` module with minimal dependencies) that serve as standardized extension points (Service Provider Interfaces - SPIs) for plugins, **so that** different parts of the EAF and applications based on it can be extended consistently and type-safely.
* **Acceptance Criteria (ACs):**
    1. At least 2-3 distinct, business-relevant extension points are identified for the MVP and defined in the `eaf-plugin-api` module (or `eaf-core`) (e.g., `TenantLifecycleListener` to react to tenant events, `CustomCommandValidator` for additional command validation logic, `UIMenuItemProvider` to dynamically extend navigation menus in EAF-based UIs).
    2. For each extension point, a clear, well-documented Java/Kotlin interface is defined. The interface methods are precisely named, their parameters and return types are clearly typed, and expected behavior and potential exceptions are specified (KDoc/JavaDoc).
    3. These interfaces are designed as a stable, public API for plugin developers. Considerations for versioning SPIs and avoiding breaking changes are noted in the design documentation.
    4. Initial documentation for these extension point interfaces is created, explaining their purpose, typical use cases, and basic implementation guidelines.
    5. The EAF's core logic (in relevant modules like `eaf-core`, `eaf-iam`, etc.) is refactored or designed to discover, load, and securely invoke implementations of these interfaces using Java's `ServiceLoader` mechanism at appropriate points in the program flow. Errors during the invocation of a plugin method (e.g., exception in plugin code) must not jeopardize the core functionality of the EAF (e.g., through `try-catch` blocks and appropriate logging).

**Story 7.2: Implement Plugin Discovery and Loading Mechanism**

* **As an** EAF Developer, **I want** the `eaf-plugin-system` module to implement a robust mechanism for discovering, loading, and managing instances of plugins at runtime (or application startup) using Java's `ServiceLoader`, **so that** EAF-based applications can be easily and standardly extended.
* **Acceptance Criteria (ACs):**
    1. The `eaf-plugin-system` module (or a core component in `eaf-core`) contains logic that uses `ServiceLoader.load(MyExtensionPointInterface.class)` to find all plugin implementations registered in the classpath for the extension points defined in Story 7.1.
    2. Discovered plugin instances are managed. This includes:
        * Secure initialization of plugin instances (considering Dependency Injection if plugins are Spring Beans, which requires additional configuration with `ServiceLoader`, or ensuring plugins have simple, parameterless constructors).
        * Making plugin instances available to the EAF's core logic (e.g., via a registry or direct passing to consuming components).
    3. The system handles scenarios appropriately where no plugins are found for an extension point (does not lead to errors but operates with default behavior). If multiple plugins implement the same point, either all are invoked (e.g., for listeners) or there is a defined strategy for selection or prioritization (if only one implementation is allowed – for MVP, all are invoked if sensible).
    4. Informative logging (DEBUG or INFO level) is implemented to indicate which plugins for which extension points are discovered, loaded, and, if applicable, initialized. Errors during loading or initialization of a plugin are clearly logged (WARN or ERROR) and do not cause the main application to crash; instead, the faulty plugin is skipped.
    5. The process by which a plugin (as a separate JAR/Gradle module) registers its service implementations for discovery by `ServiceLoader` (i.e., by creating a file in the `META-INF/services/` directory with the fully qualified name of the interface, containing the fully qualified name of the implementation class) is clearly and detailedly documented for plugin developers.
    6. The performance of the plugin loading process at application startup is monitored and must not disproportionately slow down the startup.

**Story 7.3: Develop a Simple Example Plugin Module**

* **As an** EAF Developer (and as a future plugin developer), **I want** a simple example plugin developed as a separate Gradle module within the monorepo, **so that** I can understand how to create, build, and integrate a plugin with the EAF and how it interacts with the EAF.
* **Acceptance Criteria (ACs):**
    1. A new Gradle module (e.g., `eaf-example-plugin-auditor`) is created in the monorepo as a standalone project. It only has dependencies on the `eaf-plugin-api` module (or `eaf-core` where SPIs reside) and not vice-versa.
    2. This module implements at least one of the EAF extension point interfaces defined in Story 7.1 (e.g., a `TenantLifecycleListener` that listens for tenant creation).
    3. The example plugin provides simple but clearly verifiable functionality (e.g., logs a message with tenant ID when a new tenant is created via the API from Epic 2, or contributes a dummy entry to a list managed by another EAF service).
    4. The example plugin correctly declares its service implementations in its `META-INF/services/` directory structure.
    5. When the EAF core application (e.g., `eaf-core` or a dedicated test application that loads plugins) starts and the `eaf-example-plugin-auditor` module (as a JAR) is included in its classpath, the plugin is discovered and loaded by the `eaf-plugin-system` (Story 7.2).
    6. The functionality of the example plugin is demonstrably invoked during corresponding EAF actions (e.g., creating a tenant) and is observable (e.g., through the logged message or the contributed dummy entry).
    7. The example plugin includes its own unit tests for its internal logic.
    8. The structure, build process (`build.gradle.kts`), and configuration of the example plugin are minimal and serve as a clear template for the future development of other plugins.

**Story 7.4: Document EAF Plugin Development Process**

* **As a** Developer intending to create a plugin for an EAF-based application, **I want** clear and comprehensive documentation on how to develop, configure, build, package, and deploy a plugin in an EAF application, **so that** I can effectively, securely, and standard-compliantly extend the application's functionality.
* **Acceptance Criteria (ACs):**
    1. The developer documentation (see Epic 10) includes a dedicated section on plugin development.
    2. This section describes in detail all officially supported EAF extension point interfaces (SPIs from Story 7.1), their methods, expected behavior, and usage examples.
    3. The documentation explains the Java `ServiceLoader` mechanism, the creation of `META-INF/services/` files, and how a plugin service is correctly registered.
    4. A step-by-step guide, using the `eaf-example-plugin-auditor` (from Story 7.3) as a reference, leads the developer through the entire plugin creation process, from module definition in the Gradle build to verifying plugin functionality.
    5. Important considerations for plugin developers are covered:
        * Best practices for designing plugin implementations (e.g., statelessness, performance aspects, error handling within the plugin to avoid impacting the host system).
        * Dependency management for plugins (how to minimize conflicts with EAF-internal or other plugin dependencies).
        * Basic guidance on versioning plugins in relation to the EAF version and SPI versions.
        * Security aspects (e.g., that plugins run in the same security context as the EAF and the resulting responsibilities for the plugin developer).
    6. A troubleshooting guide for common problems during plugin development or integration is included.
    7. The documentation is current with the implemented functionality and is updated upon changes to SPIs or the loading mechanism.

---

**Epic 8: Advanced IAM - External Authentication Providers**
*Description:* Extends the `eaf-iam` module to support configuration of external authentication providers (LDAP/AD, OAuth2/OIDC, SAML2) on a per-tenant basis.
*Value:* Offers flexible authentication options for enterprise customers.

**Story 8.1: Define & Persist External Authentication Provider Configuration (per Tenant)**

* **As an** EAF Developer, **I want** to define a data model and persistence mechanisms for tenant-specific external authentication provider configurations (LDAP/AD, OAuth2/OIDC, SAML2), **so that** tenants can securely and flexibly set up their preferred identity providers.
* **Acceptance Criteria (ACs):**
    1. Generic and specific data models for `ExternalAuthProviderConfig` are defined within the `eaf-iam` module. A base entity includes common attributes (`id`, `tenantId`, `providerType` (Enum: `LDAP`, `OIDC`, `SAML`), `name` (given by tenant, unique per tenant), `isEnabled` (boolean)). Specific entities inherit from this and add provider-specific settings:
        * **LDAP/AD:** `serverUrl` (validated URL), `baseDnUsers`, `baseDnGroups`, `bindUserDn` (optional), `bindUserPassword` (encrypted at rest), `userSearchFilter`, `groupSearchFilter`, `userAttributeForUsername`, `userAttributeForEmail`, `groupAttributeForRole`, `connectionTimeoutMillis`, `readTimeoutMillis`, `useSsl/StartTls` (boolean).
        * **OAuth2/OIDC:** `clientId`, `clientSecret` (encrypted at rest), `authorizationEndpointUrl`, `tokenEndpointUrl`, `userInfoEndpointUrl` (optional), `jwkSetUri` (optional), `issuerUrl` (for OIDC Discovery), `defaultScopes` (comma-separated list), `userNameAttribute` (from UserInfo/ID Token), `emailAttribute`, `groupsClaimName` (for role mapping).
        * **SAML2:** `idpMetadataUrl` (URL to IdP metadata XML) OR `idpEntityId`, `idpSsoUrl`, `idpX509Certificate` (PEM format), `spEntityId` (generated/configurable by EAF), `spAcsUrl` (Assertion Consumer Service URL, provided by EAF), `nameIdPolicyFormat`, `attributeConsumingServiceIndex` (optional), `attributesForUsername`, `attributesForEmail`, `attributesForGroups`.
    2. Each configuration is uniquely associated with a `tenantId`. A tenant can have multiple configurations of the same or different types (e.g., two LDAP servers, one OIDC provider).
    3. A PostgreSQL table (or multiple normalized tables) is created via idempotent schema migration scripts (including rollback) to store these configurations. Sensitive information (client secrets, bind passwords) is strongly encrypted before persistence (e.g., using AES-GCM with a securely managed master key – not in code!).
    4. Backend services in the `eaf-iam` module for CRUD operations of these configurations are implemented, including validation of all specific parameters (e.g., valid URLs, correct formats).
    5. Unit tests cover the creation, validation, and secure storage/retrieval (including encryption/decryption) of configurations for each provider type. Error cases for invalid configurations are tested.

**Story 8.2: Control Plane API for Managing External Auth Provider Configurations**

* **As a** Tenant Administrator (via Control Plane API), **I want** to configure and manage external authentication providers for my tenant, **so that** my users can log in with their existing enterprise credentials.
* **Acceptance Criteria (ACs):**
    1. RESTful API endpoints are provided by the `eaf-iam` module (e.g., under `/api/controlplane/tenants/{tenantId}/auth-providers`) and are secured by appropriate permissions for tenant administrators.
    2. The endpoints support full CRUD operations (POST, GET list, GET details, PUT, DELETE) for LDAP/AD, OAuth2/OIDC, and SAML2 provider configurations for the tenant specified in the path. The PUT method updates the entire configuration; PATCH may be offered for partial updates (e.g., only `isEnabled` status).
    3. The API allows enabling (`isEnabled=true`) and disabling (`isEnabled=false`) specific provider configurations for a tenant. Only enabled providers are considered in the login process.
    4. Sensitive information (e.g., client secrets, bind passwords) is handled securely in API requests/responses (e.g., passed only on creation or explicit secret update, never returned in GET responses – instead, placeholders like "*******" or status "set/not set" are used).
    5. The API validates all incoming configuration data server-side against the models defined in Story 8.1 and returns detailed HTTP 400 responses (RFC 7807 Problem Details) in case of errors.
    6. Current API documentation (OpenAPI 3.x) is available for these endpoints, describing all parameters, schemas, and security requirements.
    7. Integration tests cover all API endpoints, including various configuration scenarios, validation errors, and authorization checks.
    8. All changes to external authentication provider configurations are recorded in the audit log.

**Story 8.3: EAF Integration with LDAP/Active Directory Authentication**

* **As a** User belonging to a tenant configured with LDAP/AD, **I want** to authenticate to EAF-based applications using my LDAP/AD credentials, **so that** I don't need a separate EAF password and can use Single Sign-On within my organization.
* **Acceptance Criteria (ACs):**
    1. The EAF's authentication flow (e.g., via a custom Spring Security `AuthenticationProvider`) can delegate authentication to one or more LDAP/AD providers configured and enabled for the tenant. The selection of the provider to use (if multiple are configured) is based on criteria (e.g., user's email domain, explicit selection in login form).
    2. The EAF connects securely (supports LDAPS/StartTLS) to the configured LDAP/AD server, searches for the user based on the configured search filter, and validates the user-provided credentials via a bind attempt.
    3. Upon successful LDAP/AD authentication, an EAF session/access token (JWT) is created for the user (similar to Story 3.3).
    4. User attributes (e.g., email, first name, last name, phone number) are mapped from LDAP/AD attributes to the EAF user representation according to a configurable mapping definition. A shadow `LocalUser` account is JIT (Just-In-Time) provisioned or an existing one is updated (status, attributes) on first successful login.
    5. Basic role mapping from LDAP/AD group memberships to EAF roles (from Story 3.4) is supported. The mapping configuration (LDAP group to EAF role) is part of the LDAP provider configuration.
    6. Configuration and use of multiple LDAP/AD servers per tenant are possible and correctly handled in the login process.
    7. Robust error handling for LDAP connectivity issues (e.g., server unreachable, timeout, certificate errors), authentication errors (wrong password, user not found, account locked in AD), and configuration errors is implemented, providing understandable (but secure) error messages to the user if applicable. All errors are logged in detail server-side.
    8. LDAP integration is covered by integration tests (using a test LDAP server, e.g., Docker-based).

**Story 8.4: EAF Integration with OAuth 2.0 / OpenID Connect (OIDC) Authentication**

* **As a** User belonging to a tenant configured with an OIDC provider, **I want** to authenticate to EAF-based applications using that OIDC provider (e.g., company's SSO, Google, Microsoft), **so that** I can leverage existing login sessions and benefit from the IdP's security features.
* **Acceptance Criteria (ACs):**
    1. The EAF's authentication flow (using Spring Security's OAuth2/OIDC client support) can redirect users to the OIDC provider configured and enabled for the tenant for authentication (Authorization Code Flow with PKCE is preferred).
    2. The EAF securely handles the OIDC callback, validates the ID token (signature, issuer, audience, nonce, expiration), exchanges the authorization code for an access token, and optionally calls the UserInfo endpoint.
    3. Upon successful OIDC authentication, an EAF session/access token (JWT) is created for the user.
    4. User attributes (according to the configured attribute mappings from `userNameAttribute`, `emailAttribute`, etc., in the OIDC provider configuration) from the ID token's claims or the UserInfo response are mapped to the EAF user representation (JIT provisioning/update of a shadow `LocalUser` account).
    5. Basic role mapping from OIDC claims (e.g., `groups`, `roles`, or custom claims) to EAF roles is supported. The mapping is part of the OIDC provider configuration.
    6. Configuration of multiple OIDC providers per tenant is possible (e.g., displaying multiple "Login with..." buttons).
    7. Robust error handling for all steps of the OIDC flow (e.g., errors from IdP, token validation errors, network issues) is implemented. Errors are logged and, if applicable, displayed to the user.
    8. OIDC integration is covered by integration tests (possibly with a mock IdP or a configurable test IdP). Security aspects like state parameter validation against CSRF are implemented.

**Story 8.5: EAF Integration with SAML 2.0 Authentication**

* **As a** User belonging to a tenant configured with a SAML IdP, **I want** to authenticate to EAF-based applications using that SAML IdP, **so that** I can use enterprise federated authentication and Single Sign-On.
* **Acceptance Criteria (ACs):**
    1. The EAF's authentication flow (using Spring Security's SAML support) can act as a SAML Service Provider (SP) and integrate with tenant-configured SAML Identity Providers (IdPs) (SP-initiated SSO Flow). The EAF provides its own SP metadata.
    2. The EAF can generate SAML authentication requests (AuthnRequests), redirect users to the IdP, and securely receive and validate incoming SAML responses (Assertions) (signature, conditions, audience restriction, subject confirmation).
    3. Upon successful SAML authentication, an EAF session/access token (JWT) is created for the user.
    4. User attributes from the SAML assertion (according to configured attribute mappings) are mapped to the EAF user representation (JIT provisioning/update of a shadow `LocalUser` account).
    5. Basic role mapping from SAML attributes/groups to EAF roles is supported. The mapping is part of the SAML provider configuration.
    6. Configuration of multiple SAML IdPs per tenant is possible.
    7. Robust error handling for all steps of the SAML flow (e.g., invalid assertion, error from IdP, configuration errors) is implemented. Errors are logged and, if applicable, displayed to the user.
    8. SAML integration is covered by integration tests (possibly with a mock IdP or a configurable test IdP like simplesamlphp). Aspects like secure exchange of certificates and metadata are considered.

**Story 8.6: Control Plane UI for Managing External Auth Provider Configurations**

* **As a** Tenant Administrator, **I want** a UI section in the Control Plane to configure and manage LDAP/AD, OAuth2/OIDC, and SAML2 authentication providers for my tenant, **so that** I can visually set up external identity integration and control the login process for my users.
* **Acceptance Criteria (ACs):**
    1. An "Authentication Providers" section is available in the Control Plane UI (within the tenant context) and accessible via navigation (only for authorized tenant administrators).
    2. The UI allows listing the external authentication providers already configured for the tenant, including their type (LDAP, OIDC, SAML) and activation status (`isEnabled`).
    3. Specific forms for each provider type (LDAP, OAuth2/OIDC, SAML2) are provided to add new provider configurations. These forms capture all parameters defined in Story 8.1 and offer help texts/tooltips for complex fields. Client-side validation for required fields and formats is performed.
    4. The UI allows editing and deleting/disabling existing provider configurations. Sensitive fields like client secrets or bind passwords are not displayed during editing but can be re-set ("Change" option).
    5. The UI clearly displays the activation status of each provider and allows it to be changed. It ensures that not all authentication methods are accidentally disabled, leaving no way to log in (e.g., at least one local admin access or one provider must remain active if it's the only login option).
    6. User interactions are intuitive and follow the "React-Admin" style. Loading states and error messages (both client-side validation errors and server-side API errors) are clearly and contextually displayed to the user.
    7. For each provider type, a "Test Connection" or "Validate Configuration" feature (if technically feasible and securely implementable, e.g., for LDAP basic bind) could be offered to check the configuration before activation.
    8. The order in which enabled providers are potentially offered to the user on a login page is configurable (if applicable).

---

**Epic 9: Advanced Licensing - Hardware Binding & Online Activation**
*Description:* Enhances `eaf-licensing` with hardware-bound license capabilities (e.g., CPU cores for ppc64le). Implements (or scaffolds) the "License Activation Server" as an internal EAF application for online license activation.
*Value:* Supports more complex licensing models and online activation.

**Story 9.1: Mechanism for Hardware Parameter Collection (ppc64le CPU Cores)**

* **As an** EAF Developer, **I want** the `eaf-licensing` module to provide a mechanism for an EAF-based application to collect relevant hardware parameters, specifically the number of manageable CPU cores on an IBM POWER (ppc64le) system, **so that** this information can be used for hardware-bound licensing.
* **Acceptance Criteria (ACs):**
    1. A method/service is implemented within `eaf-licensing` (or a system utility module accessible by `eaf-licensing`) to reliably determine the number of active/licensed CPU cores on the ppc64le system where the EAF application is running. The method must be specific to Linux on ppc64le (e.g., by parsing `/proc/cpuinfo` or using system-level commands).
    2. The mechanism is designed to be as tamper-resistant as reasonably possible within a software-only solution. The limitations of tamper resistance are documented.
    3. The collected hardware information (number of CPU cores) can be reliably retrieved by the EAF's license validation logic in a standardized format.
    4. The method for hardware collection is documented for EAF application developers, including any necessary operating system permissions or configurations for the application.
    5. Robust error handling is implemented for cases where hardware information cannot be retrieved or the result is ambiguous (e.g., the system is not a ppc64le architecture, `/proc/cpuinfo` is not readable). In such cases, a defined error value or an exception is returned.
    6. The behavior upon dynamic changes in CPU core count during application runtime (e.g., for VMs scaled live) is defined (e.g., license check occurs at startup and/or periodically; a change may trigger a license violation).
    7. Unit tests (possibly with mocked system calls) and integration tests on a ppc64le test environment validate the correct collection of CPU core count and error behavior.

**Story 9.2: Extend License Entity & Generation for Hardware Binding**

* **As an** ACCI Licensing Manager (via Control Plane backend API), **I want** to generate licenses that include hardware-binding parameters (e.g., max CPU cores), **so that** product usage can be limited based on system hardware.
* **Acceptance Criteria (ACs):**
    1. The `License` entity (from Story 5.1) is extended in the `eaf-licensing` module and the `licenses` database table to include fields for hardware-binding parameters, specifically `maxAllowedCpuCores` (Integer, nullable).
    2. The backend API for license generation (from Story 5.2, `/api/controlplane/licenses`) is updated to allow specifying `maxAllowedCpuCores` when creating or updating a new license. The input is validated (e.g., positive integer, plausible limits).
    3. The generated `signedLicenseData` (for offline activation) or the data structure for online activation securely and tamper-proofly includes these hardware-binding parameters.
    4. The Control Plane UI (if already extended for license management, otherwise this is a requirement for a later UI story) allows inputting `maxAllowedCpuCores` during license definition.
    5. API documentation and internal documentation for license managers reflect this extension.
    6. Tests ensure that licenses can be correctly created and stored with and without hardware-binding parameters.

**Story 9.3: Implement Hardware-Bound License Validation in EAF Applications**

* **As a** Developer of an EAF-based application, **I want** the EAF's license validation mechanism to check hardware-binding parameters (e.g., CPU cores) against the actual system hardware, **so that** license compliance can be enforced.
* **Acceptance Criteria (ACs):**
    1. The EAF's license validation logic (from Story 5.3 and 5.4) is extended to:
        * Retrieve the hardware-binding parameters (e.g., `maxAllowedCpuCores`) stored in the activated license.
        * Collect the actual hardware parameters from the system (using Story 9.1).
        * Compare the licensed parameters with the actual system parameters (e.g., `actualCpuCores <= licensedMaxCpuCores`).
    2. The result of the hardware binding validation is clearly signaled (e.g., as part of the overall license status or as a specific violation reason).
    3. EAF-based applications can use this validation result to adjust their behavior (e.g., refuse to start, limit functionality, display warnings). The EAF license API (Story 5.4) provides this information.
    4. Comprehensive test cases (both unit and integration tests on a ppc64le environment) demonstrate correct validation against matching and non-matching hardware parameters (e.g., more CPU cores than licensed, fewer CPU cores than licensed). Behavior when hardware information cannot be retrieved is also defined and tested (e.g., license considered invalid or fallback behavior occurs).
    5. Developer documentation describes how hardware binding works and how applications can react to it.

**Story 9.4: Design & Scaffolding for Online License Activation Server**

* **As an** EAF Development Team, **I want** the basic design and project scaffold for an "Online License Activation Server" (built as an EAF-based application itself), **so that** a central service for online license activation and validation can be developed.
* **Acceptance Criteria (ACs):**
    1. A high-level design document for the Online License Activation Server is created. It outlines core responsibilities:
        * Receiving activation requests from EAF applications (with license key and possibly hardware identifiers).
        * Validating the license key against the central database of licenses issued by ACCI (from Story 5.1/5.2).
        * Storing activation records (which license is activated on which system/hardware fingerprint and when).
        * Issuing activation confirmations or tokens to the requesting application.
        * Handling periodic re-validation requests ("pings") from activated applications.
        * Server-side mechanisms for deactivating/invalidating licenses.
    2. A new Gradle module (e.g., `eaf-license-server`) is created within the monorepo and configured as an EAF-based Spring Boot application (utilizing `eaf-core`, `eaf-observability`, etc.).
    3. Basic RESTful API endpoints for license activation (e.g., `POST /api/license/activate`) and validation (e.g., `POST /api/license/validate`) are defined (stubbed implementations, but with defined request/response structures) in the `eaf-license-server` module.
    4. The design specifies how the license server accesses the database of licenses generated by the ACCI team (from Story 5.1). For MVP, this could be the same database, but the design should also allow for a separate DB.
    5. Basic security considerations for the server are documented (protecting license data, secure communication with client applications via HTTPS, protecting server APIs from misuse).
    6. It is documented that this server will be an internally ACCI-hosted and managed application. Initial estimates for its own operating environment requirements (VM, resources) are made.
    7. The project scaffold for `eaf-license-server` includes a basic structure for services, controllers, and a README file with the design overview.

**Story 9.5: EAF Application Support for Online License Activation & Validation**

* **As a** Developer of an EAF-based application, **I want** the EAF to provide a mechanism to activate and periodically validate its license online against a central License Activation Server, **so that** licenses can be managed dynamically and misuse can be better detected.
* **Acceptance Criteria (ACs):**
    1. The `eaf-licensing` module provides an API/service for an EAF-based application to communicate securely (HTTPS) with the Online License Activation Server (from Story 9.4). The server's URL is configurable.
    2. The application can send an activation request (e.g., with a license key and unique, but anonymized, hardware identifiers based on Story 9.1, possibly hashed for privacy) to the server via this service.
    3. The application receives and securely stores the activation response from the server (e.g., an activation token, a signed confirmation, or an updated local license status). Errors from the server (e.g., invalid license key, activation limit reached) are handled and can be signaled to the application.
    4. The application can (configurably) periodically contact the server to re-validate its license (re-validation ping). The interval is configurable.
    5. The EAF's license checking tools (from Story 5.4) primarily use the status obtained from online activation/validation when this method is configured.
    6. Communication with the server includes retry mechanisms for temporary network errors.
    7. The behavior of the EAF application when the activation server is temporarily unreachable is clearly defined and configurable (e.g., a grace period based on the last successful validation, after which functionality may be restricted or warnings displayed).
    8. The online activation and validation process is documented for EAF application developers, including error handling and configuration options.
    9. Test cases (integration tests using a mock license server) demonstrate the successful online activation and validation flow, as well as behavior during server errors or unavailability.

---

**Epic 10: EAF Observability & Developer Experience (DX) Enhancements**
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
