CREATE TABLE service_accounts (
    service_account_id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    client_id VARCHAR(255) NOT NULL,
    client_secret_hash VARCHAR(255) NOT NULL,
    salt VARCHAR(255) NOT NULL,
    description VARCHAR(1024),
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMPTZ,
    CONSTRAINT fk_service_accounts_tenant_id FOREIGN KEY (tenant_id) REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    CONSTRAINT uq_service_accounts_client_id_tenant_id UNIQUE (client_id, tenant_id)
);

CREATE INDEX idx_service_accounts_tenant_id ON service_accounts(tenant_id);
CREATE INDEX idx_service_accounts_client_id ON service_accounts(client_id);

-- Assuming a join table for roles as per AC7 and Subtask 5.1
CREATE TABLE service_account_roles (
    service_account_id UUID NOT NULL,
    role_id UUID NOT NULL,
    PRIMARY KEY (service_account_id, role_id),
    CONSTRAINT fk_sar_service_account_id FOREIGN KEY (service_account_id) REFERENCES service_accounts(service_account_id) ON DELETE CASCADE,
    CONSTRAINT fk_sar_role_id FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE CASCADE -- Assuming a 'roles' table exists
);

CREATE INDEX idx_sar_service_account_id ON service_account_roles(service_account_id);
CREATE INDEX idx_sar_role_id ON service_account_roles(role_id);
