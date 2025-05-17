-- Create example tenant-specific tables for RLS testing

-- Example Notes table - simple tenant-specific note storage
CREATE TABLE IF NOT EXISTS example_notes (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(tenant_id),
    title VARCHAR(255) NOT NULL,
    content TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create index on tenant_id for performance
CREATE INDEX IF NOT EXISTS idx_example_notes_tenant_id ON example_notes(tenant_id);

-- Example Tasks table - another tenant-specific entity for testing
CREATE TABLE IF NOT EXISTS example_tasks (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(tenant_id),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    due_date TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create index on tenant_id for performance
CREATE INDEX IF NOT EXISTS idx_example_tasks_tenant_id ON example_tasks(tenant_id); 