# Epic 9: Advanced Licensing - Hardware Binding & Online Activation
>
> This document is a granulated shard from the main "ACCI-EAF-PRD.md" focusing on "Epic 9: Advanced Licensing - Hardware Binding & Online Activation".

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
