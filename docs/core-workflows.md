# 9. Core Workflow / Sequence Diagrams
>
> This document is a granulated shard from the main "ACCI-EAF-Architecture.md" focusing on "Core Workflow / Sequence Diagrams".

This section illustrates key or complex workflows using Mermaid sequence diagrams. These diagrams help in understanding the interactions between different components of the ACCI EAF and external systems.

### 9.1 User Authentication via External OIDC Provider

This sequence diagram shows the typical "Authorization Code Flow" when a user authenticates to an EAF-based application using an external OpenID Connect (OIDC) Provider. The `eaf-iam` module within the EAF-based application handles the OIDC Relying Party (RP) logic.

```mermaid
sequenceDiagram
    actor UserBrowser as User (Browser)
    participant EAFApp as EAF-based Application
    participant EAFiam as eaf-iam Module
    participant OIDCProvider as External OIDC Provider

    UserBrowser->>+EAFApp: 1. Access Protected Resource
    EAFApp->>EAFiam: 2. Verify authentication
    alt User Not Authenticated
        EAFiam->>EAFApp: 3. Initiate OIDC Authentication
        EAFApp->>EAFiam: 4. Prepare OIDC AuthN Request (construct AuthN URL)
        EAFiam-->>EAFApp: AuthN URL with client_id, redirect_uri, scope, state, nonce
        EAFApp-->>-UserBrowser: 5. Redirect to OIDC Provider (Authorization Endpoint)
    end

    UserBrowser->>+OIDCProvider: 6. Authenticates with OIDC Provider (e.g., enters credentials)
    OIDCProvider-->>-UserBrowser: 7. Redirect back to EAF App (Redirect URI with Authorization Code & state)

    UserBrowser->>+EAFApp: 8. Request to EAF App\'s Redirect URI (with Authorization Code)
    EAFApp->>EAFiam: 9. Process OIDC callback (pass Authorization Code, state)
    EAFiam->>+OIDCProvider: 10. Exchange Authorization Code for Tokens (Token Endpoint)<br/>(sends code, client_id, client_secret, redirect_uri)
    OIDCProvider-->>-EAFiam: 11. ID Token, Access Token, (Refresh Token)

    EAFiam->>EAFiam: 12. Validate ID Token (signature, issuer, audience, expiry, nonce)
    alt ID Token Valid
        opt Fetch UserInfo
            EAFiam->>+OIDCProvider: 13. Request UserInfo (UserInfo Endpoint with Access Token)
            OIDCProvider-->>-EAFiam: 14. UserInfo Response (claims)
        end
        EAFiam->>EAFApp: 15. Authentication successful, establish session<br/>(user context with claims created/updated)
        EAFApp-->>-UserBrowser: 16. Serve Protected Resource
    else ID Token Invalid
        EAFiam->>EAFApp: Authentication failed
        EAFApp-->>-UserBrowser: Show error page / Redirect to login
    end
```

**Description of the flow:**

1. **Access Protected Resource:** The user attempts to access a protected resource in an application built on ACCI EAF.
2. **Verify Authentication:** The EAF-based Application, utilizing the `eaf-iam` module, checks if the user is already authenticated.
3. **Initiate OIDC Authentication:** If the user is not authenticated, `eaf-iam` determines that OIDC authentication should be initiated (based on tenant or application configuration).
4. **Prepare OIDC AuthN Request:** `eaf-iam` constructs the URL for the OIDC Provider\'s Authorization Endpoint, including parameters like `client_id`, `redirect_uri`, requested `scope`s (e.g., `openid profile email`), a `state` parameter (for CSRF protection), and a `nonce` (for replay protection).
5. **Redirect to OIDC Provider:** The EAF-based Application redirects the user\'s browser to the OIDC Provider.
6. **User Authenticates with OIDC Provider:** The user interacts with the OIDC Provider to authenticate (e.g., enters username/password, performs MFA).
7. **Redirect back to EAF App:** Upon successful authentication, the OIDC Provider redirects the user\'s browser back to the `redirect_uri` registered by the EAF-based Application. This redirect includes an `authorization_code` and the original `state` parameter.
8. **Request to Redirect URI:** The user\'s browser makes a request to the EAF-based Application\'s redirect URI, delivering the `authorization_code`.
9. **Process OIDC Callback:** The EAF-based Application passes the `authorization_code` and `state` to `eaf-iam`. `eaf-iam` first validates the `state` parameter.
10. **Exchange Authorization Code for Tokens:** `eaf-iam` makes a direct (server-to-server) request to the OIDC Provider\'s Token Endpoint, exchanging the `authorization_code` for an ID Token, an Access Token, and optionally a Refresh Token. This request is authenticated using the EAF application\'s `client_id` and `client_secret`.
11. **ID Token, Access Token Returned:** The OIDC Provider returns the requested tokens.
12. **Validate ID Token:** `eaf-iam` meticulously validates the ID Token:
      * Signature verification using the OIDC Provider\'s public keys (obtained via JWKS URI).
      * Validation of claims like `iss` (issuer), `aud` (audience, must match `client_id`), `exp` (expiration time), `iat` (issued at time), and `nonce` (must match the one sent in step 4).
13. **(Optional) Request UserInfo:** If needed, and if an Access Token was received, `eaf-iam` can use the Access Token to request additional user claims from the OIDC Provider\'s UserInfo Endpoint.
14. **UserInfo Response:** The OIDC Provider returns the additional user claims.
15. **Authentication Successful, Establish Session:** If all validations pass, `eaf-iam` considers the user authenticated. It creates a local security context/session for the user within the EAF-based Application. User information (from ID Token and UserInfo endpoint claims) may be used to provision or update a local representation of the user within the `eaf-iam` user store for the tenant.
16. **Serve Protected Resource:** The EAF-based Application now serves the originally requested protected resource to the authenticated user.

### 9.2 Command Processing and Event Sourcing Flow

This sequence diagram illustrates the typical flow of processing a command, generating domain events, persisting these events (Event Sourcing), and updating read models (CQRS) within an application built on the ACCI EAF, utilizing Axon Framework.

```mermaid
sequenceDiagram
    participant Client as Client (e.g., UI, API Consumer)
    participant AppService as EAF App Service/API Endpoint
    participant CmdGateway as Axon Command Gateway
    participant Aggregate as Target Aggregate (e.g., TenantAggregate)
    participant EvtStore as Axon Event Store (PostgreSQL)
    participant EvtBus as Axon Event Bus
    participant Projector as Event Handler / Projector
    participant ReadDB as Read Model Database (PostgreSQL)

    Client->>+AppService: 1. Send Command (e.g., CreateTenantCommand with data)
    AppService->>CmdGateway: 2. Construct & Dispatch Command Object
    CmdGateway->>+Aggregate: 3. Route Command to @CommandHandler method
    Aggregate->>Aggregate: 4. Validate Command (against current aggregate state)
    alt Command Valid
        Aggregate->>Aggregate: 5. Apply Domain Event(s) (e.g., TenantCreatedEvent)<br/>(using AggregateLifecycle.apply())
        Note over Aggregate: Internal @EventSourcingHandler updates aggregate state
        Aggregate-->>CmdGateway: (Command handling successful)
        CmdGateway-->>AppService: 6. (Optional) Command result (e.g., aggregateId)
        AppService-->>-Client: 7. (Optional) HTTP Response (e.g., 201 Created with ID)

        Aggregate->>EvtBus: 8. Event(s) published to Event Bus (by Axon)
        EvtBus->>EvtStore: 9. Persist Event(s) (by Axon)
        EvtStore-->>EvtBus: (Persistence successful)

        EvtBus->>+Projector: 10. Event(s) delivered to @EventHandler method
        Projector->>+ReadDB: 11. Update Read Model(s) (e.g., INSERT into read_tenants)
        ReadDB-->>-Projector: (Update successful)
        Projector-->>-EvtBus: (Event processing complete)
    else Command Invalid
        Aggregate-->>CmdGateway: Exception (e.g., validation failed)
        CmdGateway-->>AppService: Propagate Exception
        AppService-->>-Client: HTTP Error Response (e.g., 400 Bad Request)
    end
```

**Description of the flow:**

1. **Send Command:** A client (e.g., a user interacting with the Control Plane UI, an external system calling an API, or another service within the EAF) initiates an action by sending a command. A command is an intent to change the state of an aggregate (e.g., `CreateTenantCommand`, `UpdateUserEmailCommand`). It typically carries the data necessary for the operation.
2. **Construct & Dispatch Command Object:** The application service or API endpoint in the EAF-based application receives the command data, constructs a formal command object (a DTO representing the command), and dispatches it through Axon\'s `CommandGateway`.
3. **Route Command to Command Handler:** The `CommandGateway` routes the command object to the appropriate `@CommandHandler` method within the designated DDD Aggregate (e.g., `TenantAggregate`). Axon Framework ensures that the target aggregate instance is loaded from the Event Store (rehydrated from its past events) or newly created if it doesn\'t exist yet (e.g., for creation commands).
4. **Validate Command:** The `@CommandHandler` method within the aggregate contains the business logic to validate the command against the current state of the aggregate and any business rules.
5. **Apply Domain Event(s):** If the command is valid, the `@CommandHandler` does not directly change the aggregate\'s state. Instead, it makes a decision and *applies* one or more domain events that represent the outcome of the command (e.g., `TenantCreatedEvent`, `UserEmailUpdatedEvent`). This is typically done using `AggregateLifecycle.apply(eventObject)` in Axon.
      * Internally, when an event is applied, a corresponding `@EventSourcingHandler` method within the same aggregate is invoked with the event. This handler is responsible for updating the aggregate\'s in-memory state based on the event\'s content.
6. **(Optional) Command Result:** After the command handler has successfully processed the command (i.e., applied events), it might return a result (e.g., the ID of the newly created aggregate, or void if no direct result is needed). This result is passed back through the `CommandGateway`.
7. **(Optional) HTTP Response:** The application service/API endpoint can then return an appropriate HTTP response to the client (e.g., HTTP 201 Created with the new resource ID, or HTTP 200 OK).
8. **Event(s) Published to Event Bus:** After the command handler method completes successfully and the unit of work is committed, Axon Framework publishes the applied domain event(s) to the `EventBus`.
9. **Persist Event(s):** Axon Framework also ensures that these domain events are durably persisted to the configured Event Store (in this case, the `DOMAINEVENTS` table in PostgreSQL via Axon\'s JDBC event storage mechanism). This is the "Event Sourcing" part.
10. **Event(s) Delivered to Event Handler:** Other components in the system, known as Event Handlers or Projectors (often annotated with `@EventHandler`), subscribe to specific types of events on the `EventBus`. When relevant events are published, Axon delivers them to these handlers.
11. **Update Read Model(s):** The Event Handler (Projector) processes the event and updates one or more read models (denormalized views of the data stored in separate tables in the Read Model Database, e.g., `read_tenants` in PostgreSQL). These read models are optimized for querying and serving data to UIs or other query clients.

This CQRS/ES flow ensures a clear separation of concerns, provides a full audit trail through the event store, and allows for flexible and scalable read model projections.

### 9.3 Tenant Creation in Detail

This sequence diagram illustrates the process of an administrator creating a new tenant via the Control Plane UI. This involves interactions between the UI, the `eaf-controlplane-api`, the `eaf-multitenancy` module (which would manage a `TenantAggregate` using Axon Framework), and potentially `eaf-iam` for setting up initial tenant-specific configurations or users (though the latter is simplified in this diagram for focus).

```mermaid
sequenceDiagram
    actor Admin as Administrator
    participant CP_UI as Control Plane UI (React)
    participant CP_API as eaf-controlplane-api
    participant CmdGateway as Axon Command Gateway (in CP_API)
    participant TenantAgg as TenantAggregate (e.g., in eaf-multitenancy)
    participant EvtStore as Axon Event Store (PostgreSQL)
    participant EvtBus as Axon Event Bus
    participant TenantProjector as TenantReadModelProjector
    participant ReadDB as Read Model DB (PostgreSQL)

    Admin->>+CP_UI: 1. Fills Tenant Creation Form (name, description, etc.)
    CP_UI->>+CP_API: 2. POST /controlplane/api/v1/tenants (with tenant data)
    CP_API->>CmdGateway: 3. Construct & Dispatch CreateTenantCommand
    CmdGateway->>+TenantAgg: 4. Route Command to @CommandHandler (new Aggregate instance)
    TenantAgg->>TenantAgg: 5. Validate Command (e.g., unique name if required)
    alt Command Valid
        TenantAgg->>TenantAgg: 6. Apply TenantCreatedEvent (with tenantId, name, etc.)
        Note over TenantAgg: @EventSourcingHandler updates aggregate state
        TenantAgg-->>CmdGateway: (Command handling successful, returns tenantId)
        CmdGateway-->>CP_API: 7. tenantId returned
        CP_API-->>-CP_UI: 8. HTTP 201 Created (with tenantId and representation)

        TenantAgg->>EvtBus: 9. TenantCreatedEvent published (by Axon)
        EvtBus->>EvtStore: 10. Persist TenantCreatedEvent (by Axon)
        EvtStore-->>EvtBus: (Persistence successful)

        EvtBus->>+TenantProjector: 11. TenantCreatedEvent delivered
        TenantProjector->>+ReadDB: 12. Insert new tenant record into \'read_tenants\' table
        ReadDB-->>-TenantProjector: (Update successful)
        TenantProjector-->>-EvtBus: (Event processing complete)
        
        CP_UI-->>-Admin: 13. Display Success (Tenant Created)
        
        Note right of CP_API: Optionally, CP_API could now issue<br/>a subsequent command to eaf-iam<br/>to create a default admin user for this new tenant.
    else Command Invalid
        TenantAgg-->>CmdGateway: Exception (e.g., validation failed)
        CmdGateway-->>CP_API: Propagate Exception
        CP_API-->>-CP_UI: HTTP Error (e.g., 400 Bad Request with error details)
        CP_UI-->>-Admin: Display Error
    end
```

**Description of the flow:**

1. **Fill Form:** An administrator uses the Control Plane UI to fill in the details for creating a new tenant (e.g., name, description).
2. **Submit Request:** The UI sends a `POST` request with the tenant data to the `eaf-controlplane-api`.
3. **Dispatch Command:** The API controller in `eaf-controlplane-api` receives the request, validates it, constructs a `CreateTenantCommand` object, and dispatches it using Axon\'s `CommandGateway`.
4. **Route to Aggregate:** The `CommandGateway` routes the command to the `@CommandHandler` method in the `TenantAggregate`. Since this is a new tenant, Axon Framework instantiates a new `TenantAggregate`.
5. **Validate Command:** The `TenantAggregate` validates the command (e.g., ensures the tenant name meets criteria, checks for uniqueness if required by business rules).
6. **Apply Event:** If valid, the `TenantAggregate` applies a `TenantCreatedEvent`, capturing all necessary data for the new tenant. The `@EventSourcingHandler` within the aggregate updates its state based on this event.
7. **Command Result:** The command handler successfully completes, potentially returning the new `tenantId`.
8. **HTTP Response to UI:** The `eaf-controlplane-api` returns a success response (e.g., HTTP 201 Created) to the Control Plane UI, including the new tenant\'s ID and representation.
9. **Event Published:** Axon Framework publishes the `TenantCreatedEvent` to the `EventBus`.
10. **Event Persisted:** Axon Framework persists the `TenantCreatedEvent` to the Event Store (PostgreSQL).
11. **Event Delivered to Projector:** The `TenantReadModelProjector`, an event handler subscribed to `TenantCreatedEvent`, receives the event.
12. **Update Read Model:** The projector creates a new record for the tenant in the `read_tenants` table (or other relevant read models) in the Read Model Database.
13. **Display Success:** The Control Plane UI informs the administrator that the tenant was successfully created.
    *Note: As indicated in the diagram, after successful tenant creation, the `eaf-controlplane-api` might initiate subsequent commands, for example, to the `eaf-iam` module to provision an initial administrator user for the newly created tenant. This follow-up action is part of the overall business process but separated for clarity in this specific aggregate\'s command flow.*

### 9.4 Online License Activation

This sequence diagram outlines the process where an EAF-based application instance performs an online activation of its license by communicating with the `eaf-license-server`. The `eaf-license-server` itself is an EAF-based application using CQRS/ES principles to manage `ActivatedLicenseAggregate`s.

```mermaid
sequenceDiagram
    participant EAFApp as EAF-based Application (Client)
    participant LicenseServerAPI as eaf-license-server (REST API)
    participant LSCmdGateway as Axon Command Gateway (in License Server)
    participant ActivatedLicAgg as ActivatedLicenseAggregate (in License Server)
    participant LSEvtStore as Axon Event Store (PostgreSQL, for License Server)
    participant LSEvtBus as Axon Event Bus (in License Server)
    participant LicProjector as LicenseReadModelProjector (in License Server)
    participant LSReadDB as Read Model DB (PostgreSQL, for License Server)

    EAFApp->>+LicenseServerAPI: 1. POST /licenseserver/api/v1/activations<br/>(productCode, licenseKey, hardwareIds, instanceId)
    Note over EAFApp, LicenseServerAPI: Application authenticates to License Server
    LicenseServerAPI->>LSCmdGateway: 2. Construct & Dispatch ActivateLicenseCommand
    LSCmdGateway->>+ActivatedLicAgg: 3. Route Command to @CommandHandler
    ActivatedLicAgg->>ActivatedLicAgg: 4. Validate Command (check licenseKey validity,<br/>activation limits, hardwareIds, etc.)
    alt Command Valid
        ActivatedLicAgg->>ActivatedLicAgg: 5. Apply LicenseActivatedEvent (with activationId, features, expiry, etc.)
        Note over ActivatedLicAgg: @EventSourcingHandler updates aggregate state
        ActivatedLicAgg-->>LSCmdGateway: (Command handling successful, returns activation details)
        LSCmdGateway-->>LicenseServerAPI: 6. Activation details (activationId, status, features)
        LicenseServerAPI-->>-EAFApp: 7. HTTP 200 OK / 201 Created (with activation details)

        ActivatedLicAgg->>LSEvtBus: 8. LicenseActivatedEvent published (by Axon)
        LSEvtBus->>LSEvtStore: 9. Persist LicenseActivatedEvent (by Axon)
        LSEvtStore-->>LSEvtBus: (Persistence successful)

        LSEvtBus->>+LicProjector: 10. LicenseActivatedEvent delivered
        LicProjector->>+LSReadDB: 11. Insert/Update \'read_activated_licenses\' record
        LSReadDB-->>-LicProjector: (Update successful)
        LicProjector-->>-LSEvtBus: (Event processing complete)
        
        EAFApp->>EAFApp: 12. Store activation details locally
    else Command Invalid
        ActivatedLicAgg-->>LSCmdGateway: Exception (e.g., license invalid, limit reached)
        LSCmdGateway-->>LicenseServerAPI: Propagate Exception
        LicenseServerAPI-->>-EAFApp: HTTP Error (e.g., 400 Bad Request with error code)
    end
```

**Description of the flow:**

1. **Request Activation:** An EAF-based application, upon initialization or when required, sends a `POST` request to the `/activations` endpoint of the `eaf-license-server`. The request includes the `productCode`, the customer\'s `licenseKey`, current `hardwareIds` (if applicable for node-locking), and a unique `instanceId` for the application instance. The application authenticates itself to the `eaf-license-server`.
2. **Dispatch Command:** The API controller in `eaf-license-server` receives the request, validates it, constructs an `ActivateLicenseCommand`, and dispatches it via its internal Axon `CommandGateway`.
3. **Route to Aggregate:** The `CommandGateway` routes the command to the `@CommandHandler` in the `ActivatedLicenseAggregate`. Axon loads or creates an aggregate instance, potentially identified by the `licenseKey` or a composite key.
4. **Validate Command:** The `ActivatedLicenseAggregate` performs validation:
      * Checks the validity and entitlements of the provided `licenseKey` (this might involve looking up a `LicenseDefinition` from its own read models or a shared configuration).
      * Verifies if the license allows further activations (e.g., checks against `maxActivations`).
      * Compares `hardwareIds` with any existing activations for the license, if hardware-binding is enforced.
5. **Apply Event:** If validation passes, the aggregate applies a `LicenseActivatedEvent`. This event contains all relevant details like a unique `activationId`, the features enabled by this license, expiration date, etc. The `@EventSourcingHandler` within the aggregate updates its state.
6. **Command Result:** The command handler returns the successful activation details.
7. **HTTP Response to Application:** The `eaf-license-server` API sends a success response (e.g., HTTP 200 OK or 201 Created) back to the EAF-based application, including the `activationId`, current `status`, list of enabled `features`, and `expiresAt` date.
8. **Event Published:** Axon Framework publishes the `LicenseActivatedEvent` to the internal `EventBus` of the `eaf-license-server`.
9. **Event Persisted:** Axon Framework persists the `LicenseActivatedEvent` to its Event Store (PostgreSQL).
10. **Event Delivered to Projector:** The `LicenseReadModelProjector` (an event handler within `eaf-license-server`) receives the event.
11. **Update Read Model:** The projector creates or updates the record for this activation in the `read_activated_licenses` table in its Read Model Database.
12. **Store Activation Locally:** The EAF-based application receives the successful activation details and should store them locally (e.g., in a configuration file, secure storage) for future offline validations and to avoid repeated online activations.
