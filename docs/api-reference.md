# 7. API Reference
>
> This document is a granulated shard from the main "ACCI-EAF-Architecture.md" focusing on "API Reference".

This section details the Application Programming Interfaces (APIs) that the ACCI EAF system will interact with, both those consumed from external sources and those provided by its own components.

### 7.1 External APIs Consumed

The ACCI EAF, particularly through its `eaf-iam` and notification capabilities, will consume the following external services/protocols:

#### 7.1.1 LDAP / Active Directory

* **Purpose:** Used by `eaf-iam` for external user authentication and to fetch user attributes (e.g., group memberships, email addresses) from an enterprise directory service.
* **Protocol:** Lightweight Directory Access Protocol (LDAP v3)
* **Connection Parameters (to be configured per deployment/tenant):**
  * LDAP Server Hostname(s): `{ldap_host}`
  * LDAP Server Port: `{ldap_port}` (e.g., 389 for LDAP, 636 for LDAPS)
  * Use SSL/TLS (LDAPS): `true/false`
  * Use StartTLS: `true/false` (if port is 389 and SSL/TLS is desired)
  * Bind DN (Service Account for searches, optional): `{bind_dn_user}`
  * Bind Password (Service Account Password, optional): `{bind_dn_password}` (Stored securely, e.g., via environment variables or mounted secrets)
  * User Search Base DN: `{user_search_base}` (e.g., `ou=users,dc=example,dc=com`)
  * User Search Filter: `{user_search_filter}` (e.g., `(&(objectClass=person)(sAMAccountName={0}))` or `(&(objectClass=inetOrgPerson)(uid={0}))`)
  * Group Search Base DN: `{group_search_base}` (e.g., `ou=groups,dc=example,dc=com`)
  * Group Search Filter: `{group_search_filter}` (e.g., `(&(objectClass=group)(member={0}))`)
  * Attribute Mappings: (Configurable map of EAF user attributes to LDAP attributes, e.g., `username -> sAMAccountName`, `email -> mail`, `displayName -> displayName`, `groups -> memberOf`)
* **Authentication Method for EAF Service Account:** Simple BIND with the configured Bind DN and password (if anonymous binding is not sufficient for searches).
* **Authentication Method for End Users:** Simple BIND operation against the LDAP server using the username (transformed via search filter) and password provided by the user.
* **Key Operations Used by `eaf-iam`:**
  * **BIND Operation:**
    * Description: To authenticate a user by attempting to bind to the LDAP server with their provided credentials.
    * Also used by the EAF service account (if configured) to establish a connection for searching.
  * **SEARCH Operation:**
    * Description: To find a user\'s DN based on their login name, and to fetch user attributes (e.g., for creating a user profile in the EAF or checking group memberships for authorization).
    * To find groups a user is a member of.
* **Data Format:** LDAP Data Interchange Format (LDIF) for entries and attributes.
* **Error Handling:** LDAP result codes (e.g., "Invalid Credentials", "No Such Object", "Server Down") will be caught and mapped to appropriate EAF internal exceptions or error responses. Connection timeouts and search timeouts must be configured.
* **Link to Official Docs:**
  * LDAP v3: [RFC 4510-4519](https://datatracker.ietf.org/doc/html/rfc4510) (and related RFCs)
  * Active Directory: Microsoft documentation for AD LDAP.

#### 7.1.2 SMTP Server

* **Purpose:** Used by various EAF modules or EAF-based applications for sending email notifications (e.g., password reset links, system alerts, license notifications).
* **Protocol:** Simple Mail Transfer Protocol (SMTP)
* **Connection Parameters (to be configured per deployment):**
  * SMTP Server Hostname: `{smtp_host}`
  * SMTP Server Port: `{smtp_port}` (e.g., 25, 465 for SMTPS, 587 for SMTP with STARTTLS)
  * Authentication Required: `true/false`
  * Username (if auth required): `{smtp_username}`
  * Password (if auth required): `{smtp_password}` (Stored securely)
  * Transport Security: None / SSL/TLS (SMTPS) / STARTTLS
  * Default Sender Address ("From"): `{default_from_address}`
* **Authentication Method:** Typically SMTP AUTH (e.g., PLAIN, LOGIN, CRAM-MD5) if required by the server.
* **Key Operations Used:**
  * **Sending an Email:**
    * Description: The EAF will construct an email message (headers, body) and transmit it to the configured SMTP server for delivery.
    * Key SMTP commands involved: `EHLO/HELO`, `MAIL FROM`, `RCPT TO`, `DATA`.
* **Data Format:** Email messages formatted according to RFC 5322 (Internet Message Format) and related MIME standards (RFC 2045-2049).
* **Error Handling:** SMTP reply codes (e.g., "550 No such user here", "421 Service not available") will be handled. Connection errors, timeouts, and authentication failures will be logged and may trigger retry mechanisms or alert administrators.
* **Link to Official Docs:**
  * SMTP: [RFC 5321](https://datatracker.ietf.org/doc/html/rfc5321)
  * MIME: [RFC 2045-2049](https://datatracker.ietf.org/doc/html/rfc2045)

#### 7.1.3 OpenID Connect (OIDC) Provider

* **Purpose:** Used by `eaf-iam` to enable external user authentication via OpenID Connect 1.0, allowing users to log in with their existing accounts from a configured OIDC Identity Provider (IdP).
* **Protocol:** OpenID Connect 1.0 (built on OAuth 2.0).
* **Interaction Type:** The EAF acts as an OIDC Relying Party (RP). The typical flow will be the Authorization Code Flow.
* **Key Endpoints & Metadata (provided by the OIDC Provider):**
  * **Discovery Endpoint (`/.well-known/openid-configuration`):**
    * Description: A well-known URI where the OIDC Provider publishes its metadata. This metadata includes URLs for other necessary endpoints (Authorization, Token, UserInfo, JWKS), supported scopes, response types, claims, and cryptographic algorithms.
    * `eaf-iam` will fetch and use this metadata to configure its interaction with the OIDC provider dynamically or at setup.
  * **Authorization Endpoint:**
    * Description: The endpoint at the OIDC Provider where the user is redirected by the EAF to authenticate and grant consent.
    * Interaction: EAF redirects the user\'s browser to this URL with parameters like `client_id`, `response_type=code`, `scope`, `redirect_uri`, `state`, `nonce`.
  * **Token Endpoint:**
    * Description: The endpoint at the OIDC Provider where the EAF (RP) exchanges an authorization code (received via redirect from Authorization Endpoint) for an ID Token, Access Token, and optionally a Refresh Token.
    * Interaction: EAF makes a direct (server-to-server) POST request to this URL with parameters like `grant_type=authorization_code`, `code`, `redirect_uri`, `client_id`, `client_secret`.
  * **UserInfo Endpoint (Optional):**
    * Description: An OAuth 2.0 protected resource at the OIDC Provider where the EAF can retrieve additional claims about the authenticated user using the Access Token obtained from the Token Endpoint.
    * Interaction: EAF makes a GET or POST request to this URL, including the Access Token in the `Authorization` header.
  * **JWKS (JSON Web Key Set) URI:**
    * Description: An endpoint where the OIDC Provider publishes its public keys (in JWK format) used to sign ID Tokens.
    * Interaction: `eaf-iam` will fetch these keys to validate the signature of received ID Tokens.
* **Authentication (EAF RP to OIDC Provider):**
  * The EAF is registered as a client with the OIDC Provider and receives a `client_id`.
  * For the Token Endpoint, the EAF authenticates using its `client_id` and a `client_secret` (or other client authentication methods like private key JWT).
* **Request/Response Data Formats:**
  * **ID Token:** A JSON Web Token (JWT) containing claims about the authentication event and the user (e.g., `iss` (issuer), `sub` (subject/user ID), `aud` (audience/client ID), `exp` (expiration), `iat` (issued at), `nonce`, `email`, `name`, `preferred_username`). The ID Token is the primary artifact for authentication.
  * **Access Token:** Usually an opaque string for the EAF, used to authorize access to the UserInfo Endpoint. Format is specific to the OIDC Provider.
  * **UserInfo Response:** JSON object containing user claims.
* **Key Configuration Parameters (per OIDC Provider / Tenant):**
  * Issuer URL (e.g., `https://idp.example.com/oidc`)
  * Client ID (obtained from OIDC Provider registration)
  * Client Secret (obtained from OIDC Provider registration, stored securely)
  * Redirection URI(s) (EAF\'s endpoint(s) where users are redirected back after authentication, e.g., `https://eaf-app.example.com/login/oauth2/code/{registrationId}`)
  * Requested Scopes (e.g., `openid`, `profile`, `email`, custom scopes)
  * Attribute Mappings (if needed, to map OIDC claims to EAF user profile attributes)
  * Preferred JWS (JSON Web Signature) algorithm for ID Token validation.
* **Error Handling:** OAuth 2.0 and OIDC specific error codes (e.g., `invalid_request`, `unauthorized_client`, `access_denied`, `invalid_grant`) returned by the OIDC Provider will be handled. ID Token validation failures (signature, issuer, audience, expiry, nonce) must lead to authentication failure.
* **Link to Official Docs:**
  * OpenID Connect Core 1.0: [https://openid.net/specs/openid-connect-core-1_0.html](https://openid.net/specs/openid-connect-core-1_0.html)
  * OpenID Connect Discovery 1.0: [https://openid.net/specs/openid-connect-discovery-1_0.html](https://openid.net/specs/openid-connect-discovery-1_0.html)

#### 7.1.4 SAML 2.0 Identity Provider (IdP)

* **Purpose:** Used by `eaf-iam` to enable external user authentication via SAML 2.0 Web Browser SSO Profile, allowing users to log in using their existing enterprise identities.
* **Protocol:** SAML (Security Assertion Markup Language) 2.0.
* **Interaction Type:** The EAF acts as a SAML Service Provider (SP). The typical flow will be the Web Browser SSO Profile (often SP-initiated).
* **Key Endpoints & Metadata:**
  * **IdP Metadata:**
    * Description: An XML document provided by the SAML IdP that describes its services, endpoints (e.g., SingleSignOnService, SingleLogoutService), supported bindings (e.g., HTTP-Redirect, HTTP-POST), and X.509 certificates used for signing and encryption.
    * Interaction: `eaf-iam` (SP) consumes this metadata to configure trust and interaction parameters with the IdP. This can be provided via a URL or by uploading the metadata file.
  * **SP Metadata:**
    * Description: An XML document generated or configured by `eaf-iam` (SP) that describes its own services, ACS URL, SLO URL, entity ID, and X.509 certificates.
    * Interaction: This metadata is provided to the SAML IdP to configure the trust relationship from the IdP\'s side.
  * **SingleSignOnService (SSO) Endpoint (on IdP):**
    * Description: The IdP\'s endpoint where the EAF (SP) sends SAML AuthnRequests (Authentication Requests) or redirects the user\'s browser for authentication.
    * Bindings: Typically HTTP-Redirect or HTTP-POST.
  * **Assertion Consumer Service (ACS) Endpoint (on EAF/SP):**
    * Description: The EAF\'s endpoint that receives SAML Assertions (containing authentication statements and attributes) from the IdP via the user\'s browser (typically via HTTP-POST).
  * **SingleLogoutService (SLO) Endpoint (on IdP and SP, Optional):**
    * Description: Endpoints used to facilitate single logout, allowing a user to log out from all federated sessions.
* **Authentication & Trust:**
  * Trust is established by exchanging SAML metadata between the EAF (SP) and the IdP.
  * Messages (AuthnRequests from SP, Assertions from IdP) are typically digitally signed using XML Signature with X.509 certificates whose public keys are exchanged via metadata. Assertions may also be encrypted using XML Encryption.
* **Request/Response Data Formats:**
  * **SAML AuthnRequest:** An XML document sent by the EAF (SP) to the IdP to request user authentication.
  * **SAML Assertion:** An XML document issued by the IdP upon successful user authentication. It contains statements about the authentication event, the authenticated subject (user), attributes, and conditions under which the assertion is valid.
* **Key Configuration Parameters (per SAML IdP / Tenant):**
  * IdP Metadata URL or XML content.
  * SP Entity ID (EAF\'s unique identifier for this SAML integration, e.g., `https://eaf-app.example.com/saml/metadata`).
  * SP ACS URL (e.g., `https://eaf-app.example.com/login/saml2/sso/{registrationId}`).
  * SP private key and certificate for signing AuthnRequests and decrypting Assertions (if encrypted).
  * NameID Policy (format of the user identifier expected from the IdP).
  * Attribute Mappings (to map SAML Assertion attributes to EAF user profile attributes).
  * Binding types to use for requests and responses (e.g., HTTP-POST, HTTP-Redirect).
* **Error Handling:** SAML status codes within responses indicate success or failure. Validation failures of SAML Assertions (signature, issuer, audience, conditions, subject confirmation, replay attacks) must lead to authentication failure. Errors during protocol exchange (e.g., invalid AuthnRequest) are also possible.
* **Link to Official Docs:**
  * OASIS SAML 2.0 Standard: [https://www.oasis-open.org/standards#samlv2.0](https://www.google.com/search?q=https://www.oasis-open.org/standards%23samlv2.0) (includes Core, Bindings, Profiles specifications).

### 7.1.5 Local User Authentication Endpoint

* **Purpose:** Used by clients to authenticate local EAF users with username/password credentials.
* **Endpoint:** `POST /api/iam/auth/login`
* **Request Body Schema:**

```json
{
  "usernameOrEmail": "string", // Can be in format "user@tenantidentifier"
  "password": "string",
  "tenantHint": "string" // Optional, if not included in usernameOrEmail
}
```

* **Response Schema (200 OK):**

```json
{
  "accessToken": "string", // JWT token
  "refreshToken": "string", // Optional, for token refresh
  "tokenType": "Bearer",
  "expiresIn": 3600 // Seconds
}
```

* **Error Responses:**
  * `401 Unauthorized`: Invalid credentials or account locked/inactive
  * `400 Bad Request`: Invalid request format
* **JWT Token Structure:**
  * The JWT access token contains the following claims:
    * `sub`: User ID (UUID)
    * `tenantId`: Tenant ID (UUID)
    * `username`: Username
    * `roles`: Array of user roles
    * `exp`: Expiration timestamp
    * `iat`: Issued at timestamp
* **Security Notes:**
  * The endpoint has rate limiting to prevent brute force attacks
  * Failed login attempts lead to temporary account lockout
  * All login attempts (successful and failed) are logged in the audit log

### 7.2 Internal APIs Provided

#### 7.2.1 ACCI EAF Control Plane API (`eaf-controlplane-api`)

* **Purpose:** This API provides RESTful endpoints for administrators to manage core aspects of the ACCI EAF ecosystem. It serves as the backend for the React-based Control Plane UI, enabling operations related to tenant management, user administration within tenants, license Cconfiguration, internationalization settings, and identity provider configurations.

* **Base URL(s):**

  * Proposed: `/controlplane/api/v1` (The actual deployment URL will depend on the environment.)

* **Authentication/Authorization:**

  * **Authentication:** All endpoints are protected. Clients (i.e., the Control Plane UI used by administrators) must authenticate. This will be handled by `eaf-iam`, likely using dedicated administrative user accounts with credentials (e.g., username/password). The authentication mechanism will be token-based (e.g., JWTs issued upon successful login).
  * **Authorization:** Granular permissions will be enforced based on administrative roles (e.g., SuperAdmin, TenantAdmin) managed within `eaf-iam`. For example, some operations might only be available to SuperAdmins, while others might be delegated to TenantAdmins for their specific tenant.

* **General API Conventions:**

  * Data Format: JSON for request and response bodies.
  * Error Handling: Uses standard HTTP status codes (e.g., `400 Bad Request`, `401 Unauthorized`, `403 Forbidden`, `404 Not Found`, `500 Internal Server Error`). Error responses will include a JSON body with details (e.g., `errorCode`, `message`, `details`).
  * Idempotency: `PUT` and `DELETE` operations should be idempotent. `POST` operations for creation may not be.
  * Pagination: List endpoints will use query parameters for pagination (e.g., `page`, `size`).
  * Sorting & Filtering: List endpoints may support sorting (e.g., `sort=fieldName,asc`) and filtering based on specific field values.

* **Key Endpoints (Illustrative examples, to be fully specified by OpenAPI definitions):**

  * **Tenant Management:**

    * **`POST /tenants`**: Create a new tenant.
      * Request Body Schema: `{ "name": "string", "description"?: "string", "status": "ACTIVE"|"INACTIVE", ... }`
      * Success Response Schema (201 Created): `{ "id": "string", "name": "string", ... }` (Full tenant details)
    * **`GET /tenants`**: List all tenants (paginated, filterable).
      * Success Response Schema (200 OK): `[{ "id": "string", "name": "string", "status": "string", ... }]`
    * **`GET /tenants/{tenantId}`**: Get details of a specific tenant.
      * Success Response Schema (200 OK): `{ "id": "string", "name": "string", ... }`
    * **`PUT /tenants/{tenantId}`**: Update a tenant.
      * Request Body Schema: `{ "name"?: "string", "description"?: "string", "status"?: "ACTIVE"|"INACTIVE", ... }`
      * Success Response Schema (200 OK): `{ "id": "string", "name": "string", ... }`
    * **`DELETE /tenants/{tenantId}`**: Deactivate/delete a tenant (logical or hard delete TBD).
      * Success Response Schema (204 No Content or 200 OK with status)

  * **User Management (within a Tenant):**

    * **`POST /tenants/{tenantId}/users`**: Create a new local user within a tenant.
      * Request Body Schema: `{ "username": "string", "email"?: "string", "firstName"?: "string", "lastName"?: "string", "initialPassword"?: "string", "roles": ["string"], ... }`
      * Success Response Schema (201 Created): `{ "id": "string", "username": "string", "email"?: "string", ... }`
    * **`GET /tenants/{tenantId}/users`**: List users in a tenant.
      * Success Response Schema (200 OK): `[{ "id": "string", "username": "string", "email"?: "string", ... }]`
    * **`GET /tenants/{tenantId}/users/{userId}`**: Get details of a specific user.
    * **`PUT /tenants/{tenantId}/users/{userId}`**: Update user details (e.g., status, roles, profile info).
    * **`POST /tenants/{tenantId}/users/{userId}/reset-password`**: Initiate a password reset for a user.

  * **Service Account Management (within a Tenant):**

    * **`POST /tenants/{tenantId}/service-accounts`**: Create a service account for a tenant.
      * Request Body Schema: `{ "name": "string", "description"?: "string", "roles": ["string"] }`
      * Success Response Schema (201 Created): `{ "id": "string", "name": "string", "clientId": "string", "clientSecret"?: "string (returned on creation only)", ... }`
    * **`GET /tenants/{tenantId}/service-accounts`**: List service accounts for a tenant.
    * **`DELETE /tenants/{tenantId}/service-accounts/{accountId}`**: Delete a service account.

  * **Identity Provider Configuration (per Tenant):**

    * **`POST /tenants/{tenantId}/identity-providers`**: Configure an external IdP (LDAP, OIDC, SAML) for a tenant.
      * Request Body Schema: `{ "type": "LDAP"|"OIDC"|"SAML", "name": "string", "configuration": "object (schema varies by type)", "enabled": "boolean" }` (e.g., for LDAP: host, port, baseDN; for OIDC: issuerUrl, clientId, clientSecret)
      * Success Response Schema (201 Created): `{ "id": "string", "type": "string", "name": "string", ... }`
    * **`GET /tenants/{tenantId}/identity-providers`**: List configured IdPs for a tenant.
    * **`PUT /tenants/{tenantId}/identity-providers/{idpId}`**: Update an IdP configuration.

  * **License Management (Potentially global or assignable to tenants):**

    * **`POST /licenses`**: Create a new license definition (for ACCI Team).
      * Request Body Schema: `{ "productName": "string", "type": "TIME_LIMITED"|"FEATURE_BASED"|"HARDWARE_BOUND", "validityPeriodDays"?: "number", "features": ["string"], "maxActivations"?: "number", ... }`
    * **`GET /licenses`**: List all license definitions.
    * **`POST /tenants/{tenantId}/assigned-licenses`**: Assign/link a license to a tenant or activate a license for a tenant.
      * Request Body Schema: `{ "licenseId": "string", "activationDetails"?: "object" }`

  * **Internationalization (i18n) Management (per Tenant):**

    * **`GET /tenants/{tenantId}/i18n/languages`**: List supported/configured languages for a tenant.
    * **`PUT /tenants/{tenantId}/i18n/languages`**: Set supported languages for a tenant.
    * **`GET /tenants/{tenantId}/i18n/translations/{langCode}`**: Get all translations for a specific language for a tenant.
      * Success Response Schema (200 OK): `{ "key1": "translation1", "key2": "translation2", ... }`
    * **`PUT /tenants/{tenantId}/i18n/translations/{langCode}`**: Update/set translations for a language.
      * Request Body Schema: `{ "key1": "new_translation1", ... }`

* **Rate Limits:** To be defined, but appropriate rate limiting should be implemented to protect the API from abuse.

* **Link to Detailed API Specification:** *(Placeholder: An OpenAPI (Swagger) specification will be generated and maintained for this API as part of the development process. It will reside in `docs/api/controlplane-v1.yml` or be available via a Swagger UI endpoint.)*

#### 7.2.2 ACCI EAF License Server API (`eaf-license-server`)

* **Purpose:** This API provides centralized online services for activating, validating, and potentially deactivating licenses for applications built using the ACCI EAF. It enables scenarios where a deployed application instance needs to confirm its license status with a remote server. Licenses are generally issued at a "customer" level.

* **Base URL(s):**

  * Proposed: `/licenseserver/api/v1` (The actual deployment URL will depend on the environment.)

* **Authentication/Authorization:**

  * **Authentication:** All endpoints are strictly protected. Client applications (EAF-based applications requiring license checks) must authenticate to this server.
    * **Method 1 (Preferred):** Using client credentials (e.g., a unique `clientId` and `clientSecret` or a signed JWT) issued per EAF-based application, effectively identifying the "customer" or a specific deployment context. These credentials could be managed via the `eaf-controlplane-api` and securely distributed to the client applications. This interaction would leverage `eaf-iam` concepts.
    * **Method 2 (Alternative):** A pre-shared secret or API key specific to the deployed application instance, combined with other identifying information (e.g., product code, instance ID).
  * **Authorization:** Operations might be authorized based on the authenticated client application\'s identity (representing the customer) and the specific license key or activation ID being referenced. The server will verify if the requesting application/customer is entitled to perform the operation on the given license.

* **General API Conventions:**

  * Data Format: JSON for request and response bodies.
  * Error Handling: Uses standard HTTP status codes. Error responses will include a JSON body with details (e.g., `errorCode`, `message`, `validationErrors`).
  * Idempotency: Key operations should be designed with idempotency in mind where appropriate (e.g., re-validating an already active license should return its current status without side effects).

* **Key Endpoints (Illustrative examples, to be fully specified by OpenAPI definitions):**

  * **License Activation:**

    * **`POST /activations`**: Attempts to activate a license for a product instance (customer level).
      * Request Body Schema:

                ```json
                {
                  "productCode": "string",
                  "licenseKey": "string",
                  "hardwareIds": ["string"],
                  "instanceId": "string"
                }
                ```

      * Success Response Schema (200 OK or 201 Created):

                ```json
                {
                  "activationId": "string",
                  "status": "ACTIVE",
                  "productName": "string",
                  "licenseType": "TIME_LIMITED" | "FEATURE_BASED" | "PERPETUAL",
                  "expiresAt": "iso-datetime | null",
                  "activatedAt": "iso-datetime",
                  "features": ["string"],
                  "validationIntervalSeconds": "number | null"
                }
                ```

      * Error Response Schema (e.g., 400, 403, 404):

                ```json
                {
                  "errorCode": "string",
                  "message": "string"
                }
                ```

  * **License Validation:**

    * **`POST /validations`**: Validates the current status of an activated license. This is called periodically by the client application.
      * Request Body Schema:

                ```json
                {
                  "activationId": "string",
                  "productCode": "string",
                  "hardwareIds": ["string"],
                  "instanceId": "string"
                }
                ```

      * Success Response Schema (200 OK):

                ```json
                {
                  "activationId": "string",
                  "status": "ACTIVE" | "EXPIRED" | "INVALID" | "REVOKED",
                  "productName": "string",
                  "licenseType": "string",
                  "expiresAt": "iso-datetime | null",
                  "features": ["string"],
                  "validationIntervalSeconds": "number | null",
                  "message": "string | null"
                }
                ```

      * Error Response Schema (e.g., 400, 403, 404):

                ```json
                {
                  "errorCode": "string",
                  "message": "string"
                }
                ```

  * **License Deactivation (Optional):**

    * **`DELETE /activations/{activationId}`**: Deactivates a previously activated license for a specific product instance.
      * Request Parameters: `activationId` (path parameter).
      * Request Body Schema (Optional, may require additional proof/context):

                ```json
                {
                  "productCode": "string",
                  "instanceId": "string",
                  "reason": "string | null"
                }
                ```

      * Success Response Schema (200 OK or 204 No Content):

                ```json
                {
                  "status": "DEACTIVATED",
                  "deactivatedAt": "iso-datetime"
                }
                ```

      * Error Response Schema (e.g., 400, 403, 404):

                ```json
                {
                  "errorCode": "string",
                  "message": "string"
                }
                ```

* **Rate Limits:** To be defined. Calls to `/validations` might be frequent from many deployed instances, so appropriate strategies (e.g., per `activationId` or per client IP/application ID representing the customer) are necessary.

* **Link to Detailed API Specification:** *(Placeholder: An OpenAPI (Swagger) specification will be generated and maintained for this API as part of the development process. It will reside in `docs/api/licenseserver-v1.yml` or be available via a Swagger UI endpoint.)*
