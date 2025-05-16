# Epic 5: Core Licensing Mechanism
>
> This document is a granulated shard from the main "ACCI-EAF-PRD.md" focusing on "Epic 5: Core Licensing Mechanism".

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
