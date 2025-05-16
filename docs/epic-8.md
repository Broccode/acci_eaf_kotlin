# Epic 8: Advanced IAM - External Authentication Providers
>
> This document is a granulated shard from the main "ACCI-EAF-PRD.md" focusing on "Epic 8: Advanced IAM - External Authentication Providers".

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
