# Row-Level Security (RLS) Strategy for PostgreSQL in EAF

## Overview

This document outlines the Row-Level Security (RLS) strategy for ensuring data isolation between tenants in the EAF application. RLS is a PostgreSQL feature that restricts which rows a user can see or modify based on defined policies, enabling us to implement tenant data isolation at the database level.

## Core Principles

1. **Tenant Isolation**: Data for one tenant should never be visible or modifiable by another tenant.
2. **Automatic Enforcement**: Security should be enforced at the database level, not relying solely on application code.
3. **Consistent Implementation**: All tenant-specific tables must follow the same pattern.
4. **Performance Consideration**: RLS should be implemented with minimal performance impact.

## Implementation

### Tenant ID Column Standard

All tenant-specific tables must include:

- A `tenant_id` column of type UUID
- The column must be NOT NULL
- The column must have a foreign key reference to `tenants.tenantId`
- The column should be indexed for performance

Example schema:

```sql
CREATE TABLE example_table (
  id UUID PRIMARY KEY,
  tenant_id UUID NOT NULL REFERENCES tenants(tenant_id),
  -- other columns...
  
  -- Always create an index on tenant_id
  INDEX idx_example_table_tenant_id (tenant_id)
);
```

### Database User Roles

Two primary database user roles are defined:

1. **Application Role (`eaf_app`)**:
   - Used by normal application operations
   - RLS is enforced (`FORCE ROW LEVEL SECURITY`)
   - Cannot bypass tenant isolation

2. **Maintenance Role (`eaf_admin`)**:
   - Used only for system maintenance, migrations, and admin operations
   - Has `BYPASSRLS` attribute
   - Limited usage, typically not used by application code

### Session Variable

The PostgreSQL session variable `app.current_tenant_id` is used to identify the current tenant context:

- Set at the beginning of each database connection/transaction
- Contains the UUID of the current tenant from `TenantContextHolder`
- If no tenant context is set, the variable is set to NULL, '-1', or empty string (ensuring no tenant data access)
- Must be reset when connection is returned to the pool

### RLS Policies

The following generic RLS policies will be applied to all tenant-specific tables:

1. **SELECT Policy**:
   - Only allows rows where `tenant_id` matches the current session variable

2. **INSERT Policy**:
   - Enforces that new rows have the `tenant_id` matching the current session variable
   - Prevents insertion with an explicit different tenant_id

3. **UPDATE Policy**:
   - Only allows modification of rows where `tenant_id` matches the current session variable

4. **DELETE Policy**:
   - Only allows deletion of rows where `tenant_id` matches the current session variable

## Tables Requiring RLS

Initially, the following tables will implement RLS:

1. `example_notes` - Example tenant-specific table for testing
2. `example_tasks` - Example tenant-specific table for testing

Additional tenant-specific domain tables will be added to this list as they are defined.

## Performance Considerations

- All `tenant_id` columns must be indexed
- Complex queries involving multiple joins should be carefully analyzed with `EXPLAIN ANALYZE`
- Performance impact assessments should be done during development

## Developer Guidelines

When creating new tables:

1. Always add a `tenant_id` column if the table contains tenant-specific data
2. Ensure the column meets the standard definition (UUID, NOT NULL, FK reference)
3. Apply RLS policies to the table using the Liquibase templates
4. Add appropriate indexes, especially on the `tenant_id` column
5. Test thoroughly to ensure proper tenant isolation

## Testing RLS

Integration tests must verify:

1. Data for one tenant is never visible to another tenant
2. Operations (INSERT, UPDATE, DELETE) are properly restricted by tenant context
3. Attempts to bypass tenant isolation fail appropriately
4. System operations with the admin role can access data across tenants when needed
