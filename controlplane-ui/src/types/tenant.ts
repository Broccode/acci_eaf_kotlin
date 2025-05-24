import type { RaRecord } from 'react-admin';

/**
 * Tenant status enum values
 */
export type TenantStatus = 'PENDING_VERIFICATION' | 'ACTIVE' | 'INACTIVE' | 'SUSPENDED' | 'ARCHIVED';

/**
 * Tenant entity interface - extends RaRecord for React-Admin compatibility
 */
export interface Tenant extends RaRecord {
  id: string;
  name: string;
  description?: string;
  status: TenantStatus;
  createdAt: string;
  updatedAt: string;
}

/**
 * Create tenant request interface
 */
export interface CreateTenantRequest {
  name: string;
  description?: string;
  status?: TenantStatus;
}

/**
 * Update tenant request interface - extends RaRecord for React-Admin compatibility
 */
export interface UpdateTenantRequest extends RaRecord {
  id: string;
  name?: string;
  description?: string;
  status?: TenantStatus;
}

/**
 * Tenant list response interface for React-Admin
 */
export interface TenantListResponse {
  data: Tenant[];
  total: number;
}
