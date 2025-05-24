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

## Development

### Prerequisites

- Java 21 or higher
- PostgreSQL database (for production)
- H2 database (for development/testing - included)

### Running the Application

1. **Development Mode** (uses H2 in-memory database):

   ```bash
   # From the project root directory
   ./gradlew :eaf-controlplane-api:bootRun
   
   # Or from the eaf-controlplane-api directory
   cd eaf-controlplane-api
   ../gradlew bootRun
   ```

2. **With specific profile**:

   ```bash
   ./gradlew :eaf-controlplane-api:bootRun --args='--spring.profiles.active=dev'
   ```

3. **Building the application**:

   ```bash
   ./gradlew :eaf-controlplane-api:build
   ```

4. **Running tests**:

   ```bash
   ./gradlew :eaf-controlplane-api:test
   ```

### Application URLs

When running locally, the application will be available at:

- **API Base**: `http://localhost:8080/controlplane/api/v1`
- **Swagger UI**: `http://localhost:8080/controlplane/api/v1/swagger-ui.html`
- **OpenAPI Spec**: `http://localhost:8080/controlplane/api/v1/api-docs`

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
