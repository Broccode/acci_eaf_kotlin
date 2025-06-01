/**
 * Role type definitions for the ACCI EAF Control Plane
 */

/**
 * Represents a role that can be assigned to users or service accounts
 */
export interface Role {
  id: string;
  name: string;
  description: string;
  tenantId: string | null; // null for system-wide roles
  permissionIds: string[];
}

/**
 * Request/Response types for role API operations
 */
export interface RoleAssignment {
  userId: string;
  roleId: string;
  tenantId: string;
}

/**
 * Response type for paginated role lists
 */
export interface RoleListResponse {
  content: Role[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

/**
 * Type guard to check if a role is system-wide
 */
export const isSystemRole = (role: Role): boolean => {
  return role.tenantId === null;
};

/**
 * Type guard to check if a role is tenant-specific
 */
export const isTenantRole = (role: Role): boolean => {
  return role.tenantId !== null;
};
