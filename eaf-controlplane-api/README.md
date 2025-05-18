# EAF Control Plane API

This module provides a RESTful API for tenant management in the ACCI Enterprise Application Framework (EAF).

## Features

- Create, read, update, and deactivate tenants
- Secure API with OAuth2/JWT-based authentication and role-based authorization
- Validation of tenant data
- Pagination and filtering support for tenant listing
- Standardized error responses using RFC 7807 Problem Details
- Comprehensive API documentation via OpenAPI/Swagger
- Audit logging of all tenant operations

## API Endpoints

| HTTP Method | Endpoint            | Description                   | Security             |
|-------------|---------------------|-------------------------------|----------------------|
| POST        | /tenants            | Create a new tenant           | ADMIN role required  |
| GET         | /tenants            | List all tenants (paginated)  | ADMIN role required  |
| GET         | /tenants/{tenantId} | Get a specific tenant by ID   | ADMIN role required  |
| PUT         | /tenants/{tenantId} | Update an existing tenant     | ADMIN role required  |
| DELETE      | /tenants/{tenantId} | Deactivate a tenant (soft delete) | ADMIN role required |

## Configuration

Key configuration properties in `application.yml`:

- `server.port`: Port the server listens on (default: 8080)
- `server.servlet.context-path`: Base path for all endpoints (default: `/controlplane/api/v1`)
- `spring.datasource`: Database connection settings
- `spring.security.oauth2.resourceserver.jwt.issuer-uri`: JWT issuer URI for OAuth2 authentication

## Documentation

OpenAPI/Swagger documentation is available at:

- `/controlplane/api/v1/swagger-ui.html` - Swagger UI
- `/controlplane/api/v1/api-docs` - Raw OpenAPI specification

## Testing

The module includes:

- Unit tests with Mockito
- Integration tests with Spring MVC Test
- Test data using H2 in-memory database
