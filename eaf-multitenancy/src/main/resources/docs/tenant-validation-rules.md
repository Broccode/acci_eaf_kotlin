# Tenant Validation Rules

This document describes the validation rules for tenant attributes in the EAF multitenancy system.

## Tenant Name Validation

The tenant name is a unique identifier used to distinguish tenants from each other in a more human-readable format than UUIDs.

### Requirements

- **Uniqueness**: Each tenant name must be unique within the system.
- **Length**: Tenant names must be between 3 and 100 characters long.
- **Format**: Tenant names can only contain:
  - Alphanumeric characters (a-z, A-Z, 0-9)
  - Hyphens (-)
- **Whitespace**: Tenant names cannot contain spaces or other whitespace characters.
- **Leading/Trailing Characters**: Tenant names should not have leading or trailing hyphens.

Examples of valid tenant names:

- `acme-corp`
- `tenant123`
- `test-tenant-2023`

Examples of invalid tenant names:

- `ab` (too short)
- `tenant with spaces` (contains spaces)
- `tenant_name` (contains underscore)
- `tenant@name` (contains special character)

## Tenant Status Transitions

The tenant status represents the current state of a tenant within the system. The following transitions are allowed:

### Status Transition Rules

| From Status           | To Status                                                           |
|-----------------------|--------------------------------------------------------------------|
| PENDING_VERIFICATION  | ACTIVE, INACTIVE, SUSPENDED, ARCHIVED                               |
| ACTIVE                | INACTIVE, SUSPENDED, ARCHIVED                                       |
| INACTIVE              | ACTIVE, SUSPENDED, ARCHIVED                                         |
| SUSPENDED             | ACTIVE, INACTIVE, ARCHIVED                                          |
| ARCHIVED              | (none - terminal state)                                             |

### Status Definitions

- **PENDING_VERIFICATION**: Initial status for a newly created tenant. The tenant exists but is not yet ready for use.
- **ACTIVE**: The tenant is fully operational and can be used normally.
- **INACTIVE**: The tenant is temporarily deactivated but can be reactivated.
- **SUSPENDED**: The tenant is suspended due to policy violations or other issues. Similar to INACTIVE but with a different semantic meaning.
- **ARCHIVED**: The tenant has been "soft-deleted". This is a terminal state; archived tenants cannot be reactivated.
