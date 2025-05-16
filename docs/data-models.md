# 8. Data Models
>
> This document is a granulated shard from the main "ACCI-EAF-Architecture.md" focusing on "Data Models".

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

* **Description:** Represents a non-human actor (e.g., an application, a service) that needs to authenticate and authorize with EAF-protected resources or APIs, typically within a specific tenant\'s context. Managed by `eaf-iam` and `eaf-controlplane-api`.
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

* **Description:** Represents an instance of a license that has been activated for a customer\'s product deployment. This is the primary entity managed by the `eaf-license-server` and associated with licensing information in `eaf-licensing`.
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

The ACCI EAF will use Axon Framework\'s JDBC implementation for its Event Store, with PostgreSQL as the backing database. Axon Framework provides a standard, predefined schema for storing domain events and snapshots. The key tables include:

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
        status VARCHAR(50) NOT NULL, -- e.g., \'ACTIVE\', \'INACTIVE\'
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
        status VARCHAR(50) NOT NULL, -- e.g., \'ACTIVE\', \'LOCKED\'
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
        status VARCHAR(50) NOT NULL, -- e.g., \'ACTIVE\', \'EXPIRED\', \'REVOKED\'
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
        role_id VARCHAR(100) PRIMARY KEY, -- e.g., \'SUPER_ADMIN\', \'TENANT_ADMIN\', \'USER\'
        description TEXT,
        is_system_role BOOLEAN DEFAULT FALSE -- Indicates if it\'s a core EAF role
    );
    -- Permissions associated with roles might be in a separate iam_role_permissions table
    -- or defined in code if static for system roles.
    ```

* **`licensing_definitions` Table:** For storing master license definitions (used by `eaf-licensing`).

    ```sql
    CREATE TABLE licensing_definitions (
        license_def_id VARCHAR(36) PRIMARY KEY,
        product_name VARCHAR(255) NOT NULL,
        license_type VARCHAR(50) NOT NULL, -- e.g., \'TIME_LIMITED\', \'PERPETUAL\', \'FEATURE_BASED\'
        default_duration_days INTEGER, -- If time-limited
        default_features TEXT, -- Comma-separated or JSON array
        max_activations INTEGER,
        notes TEXT,
        created_at TIMESTAMP WITH TIME ZONE NOT NULL,
        updated_at TIMESTAMP WITH TIME ZONE NOT NULL
    );
    ```

Database schema migrations for read models and configuration/state tables will be managed using **Liquibase** (Version `4.31.1`), integrated into the build and deployment process.
