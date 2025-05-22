# Service Account Development Guide

## Overview

Service Accounts in the ACCI EAF enable secure machine-to-machine API access using OAuth 2.0 Client Credentials Grant Flow. This guide provides developers and administrators with comprehensive information on implementing, managing, and using Service Accounts.

## Architecture

### CQRS/Event Sourcing

Service Accounts are implemented using CQRS (Command Query Responsibility Segregation) and Event Sourcing with Axon Framework:

- **Commands**: Represent intentions to change state (e.g., `CreateServiceAccountCommand`)
- **Events**: Capture what has happened (e.g., `ServiceAccountCreatedEvent`)
- **Aggregates**: Handle business logic and emit events (`ServiceAccountAggregate`)
- **Projections**: Maintain read models for queries (`ServiceAccountProjection`)

### Key Components

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Controller    │───▶│  Management      │───▶│   Command       │
│   (REST API)    │    │  Service         │    │   Gateway       │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                                                        │
                                                        ▼
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Repository    │◀───│   Projection     │◀───│   Service       │
│   (Read Model)  │    │   (Event         │    │   Account       │
│                 │    │   Handler)       │    │   Aggregate     │
└─────────────────┘    └──────────────────┘    └─────────────────┘
```

## Service Account Lifecycle

### 1. Creation

**Command**: `CreateServiceAccountCommand`

```kotlin
val command = CreateServiceAccountCommand(
    serviceAccountId = UUID.randomUUID(),
    tenantId = tenantId,
    description = "API Access for External System",
    roles = setOf(roleId1, roleId2),
    expiresAt = OffsetDateTime.now().plusYears(1)
)

commandGateway.sendAndWait<ServiceAccountAggregate>(command)
```

**Generated Credentials**:

- `clientId`: Unique identifier for OAuth 2.0 authentication
- `clientSecret`: Secure secret (displayed only once)

### 2. Authentication

Service Accounts use OAuth 2.0 Client Credentials Grant:

```bash
curl -X POST http://localhost:8080/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials&client_id=CLIENT_ID&client_secret=CLIENT_SECRET&scope=api"
```

**Response**:

```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "expires_in": 3600,
  "scope": "api"
}
```

### 3. Role Management

Service Accounts can be assigned roles for fine-grained access control:

```bash
# Assign roles
curl -X POST http://localhost:8080/api/controlplane/tenants/{tenantId}/service-accounts/{serviceAccountId}/roles \
  -H "Authorization: Bearer {admin_token}" \
  -H "Content-Type: application/json" \
  -d '["role-uuid-1", "role-uuid-2"]'
```

## Security Features

### 1. Credential Security

- **Hashing**: Client secrets are hashed using BCrypt with secure salt
- **One-time Display**: Secrets are shown only during creation/rotation
- **Secure Storage**: Plaintext secrets are never persisted

### 2. Expiration Management

- **Default Expiration**: Configurable system default (e.g., 1 year)
- **Maximum Expiration**: System-enforced maximum allowed period
- **Token Validity**: JWT tokens never exceed service account expiration

### 3. Status Validation

Authentication validates:

- Service account status must be `ACTIVE`
- Service account must not be expired
- Tenant must be active and available

### 4. Audit Logging

All authentication attempts are logged with detailed audit information:

```json
{
  "timestamp": "2023-12-01T10:00:00Z",
  "eventType": "SERVICE_ACCOUNT_AUTH_SUCCESS",
  "clientId": "sa_12345",
  "serviceAccountId": "uuid",
  "tenantId": "uuid",
  "outcome": "SUCCESS"
}
```

## API Reference

### Management Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/controlplane/tenants/{tenantId}/service-accounts` | Create service account |
| GET | `/api/controlplane/tenants/{tenantId}/service-accounts` | List service accounts |
| GET | `/api/controlplane/tenants/{tenantId}/service-accounts/{id}` | Get details |
| PUT | `/api/controlplane/tenants/{tenantId}/service-accounts/{id}` | Update service account |
| DELETE | `/api/controlplane/tenants/{tenantId}/service-accounts/{id}` | Deactivate service account |
| POST | `/api/controlplane/tenants/{tenantId}/service-accounts/{id}/rotate-secret` | Rotate credentials |

### Role Management Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/controlplane/tenants/{tenantId}/service-accounts/{id}/roles` | Get assigned roles |
| POST | `/api/controlplane/tenants/{tenantId}/service-accounts/{id}/roles` | Assign roles |
| DELETE | `/api/controlplane/tenants/{tenantId}/service-accounts/{id}/roles` | Remove roles |
| PUT | `/api/controlplane/tenants/{tenantId}/service-accounts/{id}/roles` | Replace all roles |

### Authentication Endpoint

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/oauth2/token` | OAuth 2.0 token endpoint |

## JWT Token Structure

Successful authentication returns a JWT with service account specific claims:

```json
{
  "iss": "http://localhost:8080",
  "sub": "client-id-value",
  "aud": ["api"],
  "exp": 1640995200,
  "iat": 1640991600,
  "serviceAccountId": "uuid",
  "clientId": "string",
  "tenantId": "uuid",
  "roles": ["ROLE_SERVICE_ACCOUNT_uuid1", "ROLE_SERVICE_ACCOUNT_uuid2"],
  "type": "service_account"
}
```

## Configuration

### Application Properties

```yaml
# Service Account Configuration
eaf:
  iam:
    service-accounts:
      default-expiration-period: P1Y # 1 year
      max-expiration-period: P5Y     # 5 years
      require-expiration: false      # Allow unlimited service accounts
```

### Security Configuration

The OAuth 2.0 Authorization Server is configured in `SecurityConfig.kt`:

```kotlin
@Bean
fun authorizationServerSettings(): AuthorizationServerSettings {
    return AuthorizationServerSettings.builder()
        .issuer("http://localhost:8080")
        .tokenEndpoint("/oauth2/token")
        .jwkSetEndpoint("/oauth2/jwks")
        .build()
}
```

## Best Practices

### 1. Service Account Management

- **Principle of Least Privilege**: Assign only necessary roles
- **Regular Rotation**: Rotate credentials periodically
- **Expiration Dates**: Set appropriate expiration periods
- **Monitoring**: Monitor authentication logs for suspicious activity

### 2. Application Integration

- **Secure Storage**: Store credentials securely in application configuration
- **Error Handling**: Implement proper error handling for authentication failures
- **Token Refresh**: Handle token expiration gracefully
- **Logging**: Log authentication events for audit purposes

### 3. Development Workflow

```kotlin
// Example: Creating a service account for a microservice
class MicroserviceCredentialService(
    private val commandGateway: CommandGateway,
    private val serviceAccountRepository: ServiceAccountRepository
) {
    
    fun createServiceAccountForMicroservice(
        tenantId: UUID, 
        serviceName: String,
        requiredRoles: Set<UUID>
    ): ServiceAccountCredentials {
        
        val command = CreateServiceAccountCommand(
            serviceAccountId = UUID.randomUUID(),
            tenantId = tenantId,
            description = "Service Account for $serviceName",
            roles = requiredRoles,
            expiresAt = OffsetDateTime.now().plusYears(2)
        )
        
        val result = commandGateway.sendAndWait<ServiceAccountAggregate>(command)
        
        // Return credentials for one-time use
        return extractCredentialsFromEvent(result)
    }
}
```

## Troubleshooting

### Common Issues

1. **401 Unauthorized**
   - Check client credentials
   - Verify service account is active
   - Ensure service account is not expired

2. **403 Forbidden**
   - Verify assigned roles have required permissions
   - Check tenant access permissions

3. **Token Validation Errors**
   - Verify JWT signature and issuer
   - Check token expiration
   - Ensure proper audience claim

### Debug Logging

Enable debug logging for authentication:

```yaml
logging:
  level:
    com.acci.eaf.iam.config.ServiceAccountAuthenticationEventListener: DEBUG
    org.springframework.security.oauth2: DEBUG
```

## Testing

### Test Strategy

The Service Account implementation follows a comprehensive testing strategy:

1. **Unit Tests**: Test individual components in isolation using MockK
2. **Integration Tests**: Test API endpoints and OAuth 2.0 flow end-to-end
3. **Security Tests**: Validate authentication and authorization behavior
4. **Edge Case Tests**: Test validation logic and error handling

### Unit Tests

#### Service Layer Tests

Test business logic in the `DefaultServiceAccountManagementService`:

```kotlin
@Test
fun `should create service account with default expiration`() {
    // Arrange
    val command = CreateServiceAccountCommand(
        serviceAccountId = testServiceAccountId,
        tenantId = testTenantId,
        description = "Test Service Account",
        roles = setOf(testRoleId),
        expiresAt = null // Should use default
    )

    val expectedSecret = "generated-secret"
    every { commandGateway.sendAndWait<String>(any()) } returns expectedSecret

    // Act
    val result = service.createServiceAccount(command)

    // Assert
    result.serviceAccountId shouldBe testServiceAccountId
    result.clientSecret shouldBe expectedSecret
}

@Test
fun `should reject expiration beyond maximum allowed`() {
    val tooLateExpiration = OffsetDateTime.now().plusYears(10)
    val command = CreateServiceAccountCommand(
        serviceAccountId = testServiceAccountId,
        tenantId = testTenantId,
        description = "Test Service Account",
        roles = setOf(),
        expiresAt = tooLateExpiration
    )

    val exception = shouldThrow<IllegalArgumentException> {
        service.createServiceAccount(command)
    }

    exception.message shouldContain "expiration"
    exception.message shouldContain "maximum"
}
```

#### Credentials Service Tests

Test secure credential generation:

```kotlin
@Test
fun `should generate unique client IDs`() {
    // Arrange
    every { secureRandom.nextBytes(any()) } answers {
        Random().nextBytes(firstArg())
    }

    // Act
    val clientId1 = service.generateClientId()
    val clientId2 = service.generateClientId()

    // Assert
    clientId1 shouldNotBe clientId2
    clientId1 shouldMatch Regex("^[A-Za-z0-9_-]+$")
}

@Test
fun `should hash client secret correctly`() {
    val clientSecret = "test-client-secret"
    val salt = "test-salt"
    val expectedHash = "hashed-password"

    every { passwordEncoder.encode("$clientSecret$salt") } returns expectedHash

    val result = service.hashClientSecret(clientSecret, salt)

    result shouldBe expectedHash
    verify { passwordEncoder.encode("$clientSecret$salt") }
}
```

#### Aggregate Tests

Test service account aggregate behavior:

```kotlin
@Test
fun `should create service account with valid parameters`() {
    val fixture = AggregateTestFixture(ServiceAccountAggregate::class.java)
    
    fixture.givenNoPriorActivity()
        .`when`(CreateServiceAccountCommand(/* parameters */))
        .expectEvents(ServiceAccountCreatedEvent(/* expected event data */))
}
```

### Integration Tests

#### API Endpoint Tests

Test all CRUD operations with comprehensive validation:

```kotlin
@Test
fun `should create service account with valid request`() {
    val createRequest = ServiceAccountCreateRequest(
        description = "Test Service Account",
        roles = setOf(UUID.randomUUID()),
        expiresAt = OffsetDateTime.now().plusYears(1).toString()
    )

    mockMvc.perform(
        post("/api/controlplane/tenants/{tenantId}/service-accounts", testTenantId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createRequest))
    )
        .andExpect(status().isCreated)
        .andExpect(jsonPath("$.clientId").exists())
        .andExpect(jsonPath("$.clientSecret").exists())
        .andExpect(jsonPath("$.clientSecret").isNotEmpty)
}

@Test
fun `should reject service account creation with expiration beyond maximum allowed`() {
    val createRequest = ServiceAccountCreateRequest(
        description = "Service Account with too long expiration",
        roles = setOf(),
        expiresAt = OffsetDateTime.now().plusYears(10).toString()
    )

    mockMvc.perform(
        post("/api/controlplane/tenants/{tenantId}/service-accounts", testTenantId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createRequest))
    )
        .andExpect(status().isBadRequest)
        .andExpect(jsonPath("$.detail").value(containsString("expiration")))
}

@Test
fun `should list service accounts without exposing secrets`() {
    createTestServiceAccount()

    mockMvc.perform(
        get("/api/controlplane/tenants/{tenantId}/service-accounts", testTenantId)
    )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$").isArray)
        .andExpect(jsonPath("$[0].clientId").exists())
        .andExpect(jsonPath("$[0].clientSecret").doesNotExist())
        .andExpect(jsonPath("$[0].clientSecretHash").doesNotExist())
}
```

#### OAuth 2.0 Flow Tests

Test authentication flow thoroughly:

```kotlin
@Test
fun `should authenticate with valid service account credentials`() {
    mockMvc.perform(
        post("/oauth2/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("grant_type", "client_credentials")
            .param("client_id", testClientId)
            .param("client_secret", testClientSecret)
    )
    .andExpect(status().isOk)
    .andExpect(jsonPath("$.access_token").exists())
    .andExpect(jsonPath("$.token_type").value("Bearer"))
    .andExpect(jsonPath("$.expires_in").exists())
}

@Test
fun `should reject authentication with invalid credentials`() {
    mockMvc.perform(
        post("/oauth2/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("grant_type", "client_credentials")
            .param("client_id", "invalid-client-id")
            .param("client_secret", "invalid-secret")
    )
    .andExpect(status().isUnauthorized)
}

@Test
fun `should reject authentication for expired service account`() {
    createExpiredServiceAccount()
    
    mockMvc.perform(
        post("/oauth2/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("grant_type", "client_credentials")
            .param("client_id", expiredClientId)
            .param("client_secret", expiredClientSecret)
    )
    .andExpect(status().isUnauthorized)
}
```

### Test Configuration

Integration tests use dedicated test configuration:

```kotlin
@SpringBootTest
@TestPropertySource(properties = [
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "eaf.security.disable-auth=true", // For API tests
    "eaf.iam.service-accounts.default-expiration-period=P1Y",
    "eaf.iam.service-accounts.max-expiration-period=P5Y"
])
@Transactional
class ServiceAccountControllerIntegrationTest {
    // Test implementation
}
```

## Migration and Deployment

### Database Migrations

Service Account schema is managed via Liquibase:

```sql
-- liquibase formatted sql
-- changeset author:create-service-accounts-table

CREATE TABLE service_accounts (
    service_account_id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    client_id VARCHAR(255) UNIQUE NOT NULL,
    client_secret_hash VARCHAR(500) NOT NULL,
    salt VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE,
    FOREIGN KEY (tenant_id) REFERENCES tenants(tenant_id)
);
```

### Production Considerations

- **High Availability**: Ensure token validation works across instances
- **Performance**: Monitor token generation and validation performance
- **Security**: Regularly audit service account usage and permissions
- **Compliance**: Ensure audit logging meets compliance requirements

## Related Documentation

- [OAuth 2.0 Client Credentials Grant (RFC 6749)](https://tools.ietf.org/html/rfc6749#section-4.4)
- [JWT Specification (RFC 7519)](https://tools.ietf.org/html/rfc7519)
- [ACCI EAF Security Architecture](./security-architecture.md)
- [RBAC Implementation Guide](./rbac-guide.md)
