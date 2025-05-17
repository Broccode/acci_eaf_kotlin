-- Create tenants table
CREATE TABLE IF NOT EXISTS tenants (
    tenant_id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create unique index on name
CREATE UNIQUE INDEX IF NOT EXISTS idx_tenant_name ON tenants (name);

-- Comment on table
COMMENT ON TABLE tenants IS 'Stores tenant information for multitenancy support';

-- Comment on columns
COMMENT ON COLUMN tenants.tenant_id IS 'Primary key and unique identifier for the tenant';
COMMENT ON COLUMN tenants.name IS 'Unique name of the tenant';
COMMENT ON COLUMN tenants.status IS 'Current status of the tenant (PENDING_VERIFICATION, ACTIVE, INACTIVE, SUSPENDED, ARCHIVED)';
COMMENT ON COLUMN tenants.created_at IS 'Timestamp when the tenant was created';
COMMENT ON COLUMN tenants.updated_at IS 'Timestamp when the tenant was last updated'; 