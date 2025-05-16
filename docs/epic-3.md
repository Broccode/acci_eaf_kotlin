# Epic 3: Core Identity & Access Management (IAM) - Local Users & RBAC
>
> This document is a granulated shard from the main "ACCI-EAF-PRD.md" focusing on "Epic 3: Core Identity & Access Management (IAM) - Local Users & RBAC".

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
