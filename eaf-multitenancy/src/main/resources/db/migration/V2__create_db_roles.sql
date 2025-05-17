-- Create database roles for EAF application with RLS

-- Check if roles already exist and create them if they don't
DO $$
BEGIN
    -- Create application role (with RLS enforcement)
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'eaf_app') THEN
        CREATE ROLE eaf_app;
        COMMENT ON ROLE eaf_app IS 'Standard application role used for normal operations. RLS is enforced for this role.';
    END IF;

    -- Create admin role (with BYPASSRLS)
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'eaf_admin') THEN
        CREATE ROLE eaf_admin BYPASSRLS;
        COMMENT ON ROLE eaf_admin IS 'Maintenance role with BYPASSRLS for system operations and administration tasks.';
    END IF;
END
$$; 