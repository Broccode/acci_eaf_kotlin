# Epic 2: Core Multitenancy Implementation
>
> This document is a granulated shard from the main "ACCI-EAF-PRD.md" focusing on "Epic 2: Core Multitenancy Implementation".

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
