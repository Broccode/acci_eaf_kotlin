-- Create RLS policies for tenant data isolation

-- Create the app.current_tenant_id parameter if it doesn't exist
DO $$
BEGIN
    -- Check if parameter exists
    IF NOT EXISTS (SELECT 1 FROM pg_settings WHERE name = 'app.current_tenant_id') THEN
        -- Create the parameter
        PERFORM set_config('app.current_tenant_id', NULL, false);
    END IF;
END
$$;

-- Enable RLS on example_notes table
ALTER TABLE example_notes ENABLE ROW LEVEL SECURITY;
ALTER TABLE example_notes FORCE ROW LEVEL SECURITY;

-- Create policies for example_notes table
CREATE POLICY rls_select_example_notes ON example_notes 
    FOR SELECT
    USING (tenant_id::text = current_setting('app.current_tenant_id', true)::uuid);

CREATE POLICY rls_insert_example_notes ON example_notes 
    FOR INSERT
    WITH CHECK (tenant_id::text = current_setting('app.current_tenant_id', true)::uuid);

CREATE POLICY rls_update_example_notes ON example_notes 
    FOR UPDATE
    USING (tenant_id::text = current_setting('app.current_tenant_id', true)::uuid);

CREATE POLICY rls_delete_example_notes ON example_notes 
    FOR DELETE
    USING (tenant_id::text = current_setting('app.current_tenant_id', true)::uuid);

-- Enable RLS on example_tasks table
ALTER TABLE example_tasks ENABLE ROW LEVEL SECURITY;
ALTER TABLE example_tasks FORCE ROW LEVEL SECURITY;

-- Create policies for example_tasks table
CREATE POLICY rls_select_example_tasks ON example_tasks 
    FOR SELECT
    USING (tenant_id::text = current_setting('app.current_tenant_id', true)::uuid);

CREATE POLICY rls_insert_example_tasks ON example_tasks 
    FOR INSERT
    WITH CHECK (tenant_id::text = current_setting('app.current_tenant_id', true)::uuid);

CREATE POLICY rls_update_example_tasks ON example_tasks 
    FOR UPDATE
    USING (tenant_id::text = current_setting('app.current_tenant_id', true)::uuid);

CREATE POLICY rls_delete_example_tasks ON example_tasks 
    FOR DELETE
    USING (tenant_id::text = current_setting('app.current_tenant_id', true)::uuid);

-- Grant permissions to roles
GRANT USAGE ON SCHEMA public TO eaf_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON example_notes TO eaf_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON example_tasks TO eaf_app;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO eaf_app;

-- Admin role gets full access
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO eaf_admin;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO eaf_admin; 