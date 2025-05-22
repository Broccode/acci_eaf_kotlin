# Authentication and Authorization in EAF

This document describes the authentication and authorization mechanisms implemented in the ACCI EAF system.

## 1. Overview

The EAF platform implements a comprehensive security model based on these key principles:

- **Multi-tenancy**: Resources are isolated by tenant
- **Role-Based Access Control (RBAC)**: Permissions are granted via roles
- **JWT-based authentication**: Secure, stateless authentication
- **Spring Security integration**: Method-level and endpoint security

## 2. Authentication Mechanisms

### 2.1 Local User Authentication

Users can authenticate with a username/password combination stored in the EAF database:

```
POST /api/iam/auth/login
```

Request body:

```json
{
  "usernameOrEmail": "username@tenant",
  "password": "user-password",
  "tenantHint": "optional-tenant-identifier"
}
```

Response:

```json
{
  "accessToken": "jwt-token",
  "refreshToken": "refresh-token",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

The login mechanism enforces security through:

- Password hashing with bcrypt
- Account lockout after multiple failed attempts
- Audit logging of authentication events

### 2.2 External Identity Provider Authentication

EAF supports federation with external identity providers through:

- OIDC (OpenID Connect)
- SAML (Security Assertion Markup Language)
- LDAP (Lightweight Directory Access Protocol)

External authentication is configured per tenant via the Control Plane API.

### 2.3 Service Account Authentication

Applications and services can authenticate using:

- Client credentials flow
- JWT client assertion

Service accounts are managed via the Control Plane API.

## 3. JWT Token Structure

Upon successful authentication, the EAF issues a JWT token containing the user's identity and authorization information.

### 3.1 JWT Claims

```json
{
  "sub": "user-id-uuid",
  "tenantId": "tenant-id-uuid",
  "username": "username",
  "roles": ["role1", "role2"],
  "permissions": ["permission:create", "permission:read"],
  "exp": 1633031357,
  "iat": 1633027757
}
```

Key fields:

- `sub`: User identifier (UUID)
- `tenantId`: Tenant identifier (UUID)
- `username`: User's login name
- `roles`: Array of assigned role names
- `permissions`: Array of effective permissions derived from roles
- `exp`: Expiration timestamp
- `iat`: Issued-at timestamp

### 3.2 JWT Configuration

The JWT tokens are:

- Signed using HMAC-SHA256 (HS256) or RSA (RS256)
- Configured with appropriate expiration (typically 1 hour for access tokens)
- Validated on every request

## 4. Role-Based Access Control (RBAC)

The EAF implements a comprehensive RBAC system to control access to resources.

### 4.1 Core RBAC Components

#### Permissions

Permissions are the most granular level of access control, representing specific actions on resources. They follow the format `resource:action`.

Examples:

- `user:create` - Create users
- `role:read` - Read role information
- `tenant:manage` - Manage tenant configuration

Permissions are system-defined and cannot be created by tenant administrators.

#### Roles

Roles are collections of permissions that can be assigned to users or service accounts. Roles can be:

1. **System-wide (global)**: Managed by EAF super-administrators, not associated with any specific tenant
2. **Tenant-specific**: Created and managed by tenant administrators, scoped to a specific tenant

Examples:

- `System Administrator` (global)
- `Tenant Administrator` (tenant-specific)
- `Content Manager` (tenant-specific)

### 4.2 Role-Permission Assignments

Roles are assigned multiple permissions to define their capabilities:

1. **Global Roles**: EAF super-administrators can assign any permission to global roles
2. **Tenant Roles**: Tenant administrators can assign permitted permissions to tenant-specific roles

### 4.3 User-Role Assignments

Users or service accounts are assigned roles within a tenant:

1. **System-wide Roles**: Can be assigned to users across tenants by EAF super-administrators
2. **Tenant-specific Roles**: Can be assigned to users within the specific tenant

## 5. Spring Security Integration

The EAF uses Spring Security to enforce authorization at multiple levels.

### 5.1 Method Security

Method-level security is implemented using `@PreAuthorize` annotations:

```kotlin
@PreAuthorize("hasAuthority('user:create')")
fun createUser(user: User): User {
    // Method implementation
}
```

This ensures that only users with the required permission can execute the method.

### 5.2 Endpoint Security

REST API endpoints are protected through Spring Security configuration:

```kotlin
http
    .authorizeHttpRequests {
        it.requestMatchers("/api/public/**").permitAll()
        it.requestMatchers("/api/controlplane/**").authenticated()
        it.anyRequest().authenticated()
    }
    .oauth2ResourceServer {
        it.jwt { jwt -> /* JWT configuration */ }
    }
```

Endpoint-specific permissions are enforced using `@PreAuthorize` annotations on controller methods.

### 5.3 Custom Security Expressions

The EAF provides custom security expressions for complex authorization scenarios:

```kotlin
@PreAuthorize("hasAuthority('role:assign') and isMemberOfTenant(#tenantId)")
fun assignRoleToUser(tenantId: UUID, userId: UUID, roleId: UUID) {
    // Method implementation
}
```

Common expressions include:

- `isMemberOfTenant(tenantId)`: Checks if the user belongs to the specified tenant
- `hasAnyRole(roles)`: Checks if the user has any of the specified roles
- `hasAllPermissions(permissions)`: Checks if the user has all specified permissions

### 5.4 Tenant Context Security

The EAF enforces tenant isolation to ensure that users can only access resources within their tenant:

1. The `TenantSecurity` component validates tenant context in controller methods
2. Database queries are automatically filtered by tenant ID where applicable
3. Cross-tenant access attempts are blocked with 403 Forbidden responses

## 6. Security Best Practices

The EAF security implementation follows these best practices:

1. **Defense in depth**: Multiple security layers (authentication, authorization, tenant isolation)
2. **Least privilege**: Users receive only the permissions needed for their functions
3. **Strong authentication**: Password policies, multi-factor options, account lockout
4. **Audit trails**: All security-related events are logged for accountability
5. **Token security**: Short expiration times, secure storage guidance

## 7. Extending the Security Model

### 7.1 Adding New Permissions

When adding new functionality, define appropriate permissions following the `resource:action` pattern.

### 7.2 Implementing Custom Authorization Rules

For complex authorization requirements, extend the security expressions system:

```kotlin
@Bean
fun methodSecurityExpressionHandler(): MethodSecurityExpressionHandler {
    val expressionHandler = DefaultMethodSecurityExpressionHandler()
    expressionHandler.setPermissionEvaluator(customPermissionEvaluator)
    return expressionHandler
}
```

### 7.3 Future ABAC Support

The RBAC design is extensible to support Attribute-Based Access Control (ABAC) concepts in the future. The permission model can be enhanced to include:

- Contextual conditions
- Time-based restrictions
- Resource attribute-based decisions
