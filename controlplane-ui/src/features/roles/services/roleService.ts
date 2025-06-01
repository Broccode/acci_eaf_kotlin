/**
 * Role management service for API interactions
 */
import type { Role } from '../../../types/role';

// Use environment variable for API base URL, fallback to default
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api/controlplane';

/**
 * Get authentication headers for API requests
 */
const getAuthHeaders = (): HeadersInit => {
  const token = localStorage.getItem('auth_token');
  return {
    'Content-Type': 'application/json',
    'Authorization': token ? `Bearer ${token}` : '',
  };
};

/**
 * Handle API errors consistently
 */
const handleApiError = async (response: Response): Promise<never> => {
  let errorMessage = `API Error: ${response.status} ${response.statusText}`;

  try {
    const errorData = await response.json();
    if (errorData.message) {
      errorMessage = errorData.message;
    } else if (errorData.error) {
      errorMessage = errorData.error;
    }
  } catch {
    // If parsing JSON fails, use default error message
  }

  throw new Error(errorMessage);
};

/**
 * Role service for managing roles and assignments
 */
export const roleService = {
  /**
   * Get all roles assigned to a user
   */
  async getUserRoles(tenantId: string, userId: string): Promise<Role[]> {
    const response = await fetch(
      `${API_BASE_URL}/tenants/${tenantId}/users/${userId}/roles`,
      {
        method: 'GET',
        headers: getAuthHeaders(),
      }
    );

    if (!response.ok) {
      await handleApiError(response);
    }

    return response.json();
  },

  /**
   * Get all roles available for a tenant (both system-wide and tenant-specific)
   */
  async getAvailableRoles(tenantId: string): Promise<Role[]> {
    const response = await fetch(
      `${API_BASE_URL}/tenants/${tenantId}/roles/available`,
      {
        method: 'GET',
        headers: getAuthHeaders(),
      }
    );

    if (!response.ok) {
      await handleApiError(response);
    }

    // The API might return a paginated response, handle accordingly
    const data = await response.json();

    // If it's a paginated response
    if (data.content && Array.isArray(data.content)) {
      return data.content;
    }

    // If it's a direct array
    if (Array.isArray(data)) {
      return data;
    }

    throw new Error('Unexpected response format from available roles API');
  },

  /**
   * Assign a role to a user
   */
  async assignRole(tenantId: string, userId: string, roleId: string): Promise<void> {
    const response = await fetch(
      `${API_BASE_URL}/tenants/${tenantId}/users/${userId}/roles/${roleId}`,
      {
        method: 'POST',
        headers: getAuthHeaders(),
      }
    );

    if (!response.ok) {
      await handleApiError(response);
    }
  },

  /**
   * Remove a role from a user
   */
  async removeRole(tenantId: string, userId: string, roleId: string): Promise<void> {
    const response = await fetch(
      `${API_BASE_URL}/tenants/${tenantId}/users/${userId}/roles/${roleId}`,
      {
        method: 'DELETE',
        headers: getAuthHeaders(),
      }
    );

    if (!response.ok) {
      await handleApiError(response);
    }
  },

  /**
   * Update user roles (remove and add in batch)
   * This is a convenience method that handles multiple role changes
   */
  async updateUserRoles(
    tenantId: string,
    userId: string,
    currentRoleIds: string[],
    newRoleIds: string[]
  ): Promise<void> {
    // Find roles to remove
    const rolesToRemove = currentRoleIds.filter(id => !newRoleIds.includes(id));

    // Find roles to add
    const rolesToAdd = newRoleIds.filter(id => !currentRoleIds.includes(id));

    // Execute all operations
    const operations: Promise<void>[] = [];

    // Remove roles
    for (const roleId of rolesToRemove) {
      operations.push(this.removeRole(tenantId, userId, roleId));
    }

    // Add roles
    for (const roleId of rolesToAdd) {
      operations.push(this.assignRole(tenantId, userId, roleId));
    }

    // Wait for all operations to complete
    await Promise.all(operations);
  },
};
