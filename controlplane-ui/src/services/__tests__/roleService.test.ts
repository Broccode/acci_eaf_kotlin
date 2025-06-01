import { vi } from 'vitest';
import type { Role } from '../../types/role';
import { roleService } from '../roleService';

// Mock fetch globally
global.fetch = vi.fn();

const mockRoles: Role[] = [
  {
    id: 'role-1',
    name: 'System Administrator',
    description: 'Full system access',
    tenantId: null,
    permissionIds: ['perm-1', 'perm-2'],
  },
  {
    id: 'role-2',
    name: 'Tenant Admin',
    description: 'Tenant administration',
    tenantId: 'test-tenant-id',
    permissionIds: ['perm-3'],
  },
];

describe('roleService', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
    localStorage.setItem('auth_token', 'test-token');
  });

  describe('getUserRoles', () => {
    it('should fetch user roles successfully', async () => {
      const mockResponse = new Response(JSON.stringify(mockRoles), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
      });
      vi.mocked(global.fetch).mockResolvedValueOnce(mockResponse);

      const result = await roleService.getUserRoles('tenant-1', 'user-1');

      expect(global.fetch).toHaveBeenCalledWith(
        '/api/controlplane/tenants/tenant-1/users/user-1/roles',
        {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer test-token',
          },
        }
      );
      expect(result).toEqual(mockRoles);
    });

    it('should handle API errors', async () => {
      const mockResponse = new Response(
        JSON.stringify({ message: 'User not found' }),
        {
          status: 404,
          headers: { 'Content-Type': 'application/json' },
        }
      );
      vi.mocked(global.fetch).mockResolvedValueOnce(mockResponse);

      await expect(roleService.getUserRoles('tenant-1', 'user-1')).rejects.toThrow('User not found');
    });
  });

  describe('getAvailableRoles', () => {
    it('should fetch available roles successfully (direct array)', async () => {
      const mockResponse = new Response(JSON.stringify(mockRoles), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
      });
      vi.mocked(global.fetch).mockResolvedValueOnce(mockResponse);

      const result = await roleService.getAvailableRoles('tenant-1');

      expect(global.fetch).toHaveBeenCalledWith(
        '/api/controlplane/tenants/tenant-1/roles/available',
        {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer test-token',
          },
        }
      );
      expect(result).toEqual(mockRoles);
    });

    it('should handle paginated response', async () => {
      const paginatedResponse = {
        content: mockRoles,
        totalElements: 2,
        totalPages: 1,
      };
      const mockResponse = new Response(JSON.stringify(paginatedResponse), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
      });
      vi.mocked(global.fetch).mockResolvedValueOnce(mockResponse);

      const result = await roleService.getAvailableRoles('tenant-1');

      expect(result).toEqual(mockRoles);
    });

    it('should throw error for unexpected response format', async () => {
      const mockResponse = new Response(JSON.stringify({ unexpected: 'format' }), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
      });
      vi.mocked(global.fetch).mockResolvedValueOnce(mockResponse);

      await expect(roleService.getAvailableRoles('tenant-1')).rejects.toThrow(
        'Unexpected response format from available roles API'
      );
    });
  });

  describe('assignRole', () => {
    it('should assign role successfully', async () => {
      const mockResponse = new Response(null, { status: 204 });
      vi.mocked(global.fetch).mockResolvedValueOnce(mockResponse);

      await roleService.assignRole('tenant-1', 'user-1', 'role-1');

      expect(global.fetch).toHaveBeenCalledWith(
        '/api/controlplane/tenants/tenant-1/users/user-1/roles/role-1',
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer test-token',
          },
        }
      );
    });

    it('should handle assignment errors', async () => {
      const mockResponse = new Response(
        JSON.stringify({ error: 'Role already assigned' }),
        {
          status: 409,
          headers: { 'Content-Type': 'application/json' },
        }
      );
      vi.mocked(global.fetch).mockResolvedValueOnce(mockResponse);

      await expect(roleService.assignRole('tenant-1', 'user-1', 'role-1')).rejects.toThrow(
        'Role already assigned'
      );
    });
  });

  describe('removeRole', () => {
    it('should remove role successfully', async () => {
      const mockResponse = new Response(null, { status: 204 });
      vi.mocked(global.fetch).mockResolvedValueOnce(mockResponse);

      await roleService.removeRole('tenant-1', 'user-1', 'role-1');

      expect(global.fetch).toHaveBeenCalledWith(
        '/api/controlplane/tenants/tenant-1/users/user-1/roles/role-1',
        {
          method: 'DELETE',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer test-token',
          },
        }
      );
    });
  });

  describe('updateUserRoles', () => {
    it('should handle adding and removing roles', async () => {
      const mockResponse = new Response(null, { status: 204 });
      vi.mocked(global.fetch).mockResolvedValue(mockResponse);

      const currentRoleIds = ['role-1', 'role-2'];
      const newRoleIds = ['role-2', 'role-3'];

      await roleService.updateUserRoles('tenant-1', 'user-1', currentRoleIds, newRoleIds);

      // Should remove role-1
      expect(global.fetch).toHaveBeenCalledWith(
        '/api/controlplane/tenants/tenant-1/users/user-1/roles/role-1',
        {
          method: 'DELETE',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer test-token',
          },
        }
      );

      // Should add role-3
      expect(global.fetch).toHaveBeenCalledWith(
        '/api/controlplane/tenants/tenant-1/users/user-1/roles/role-3',
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer test-token',
          },
        }
      );

      expect(global.fetch).toHaveBeenCalledTimes(2);
    });

    it('should handle no changes', async () => {
      const roleIds = ['role-1', 'role-2'];

      await roleService.updateUserRoles('tenant-1', 'user-1', roleIds, roleIds);

      expect(global.fetch).not.toHaveBeenCalled();
    });

    it('should handle errors in batch operations', async () => {
      const mockSuccessResponse = new Response(null, { status: 204 });
      const mockErrorResponse = new Response(
        JSON.stringify({ message: 'Failed to assign role' }),
        {
          status: 400,
          headers: { 'Content-Type': 'application/json' },
        }
      );

      vi.mocked(global.fetch)
        .mockResolvedValueOnce(mockSuccessResponse) // Remove succeeds
        .mockResolvedValueOnce(mockErrorResponse); // Add fails

      await expect(
        roleService.updateUserRoles('tenant-1', 'user-1', ['role-1'], ['role-2'])
      ).rejects.toThrow('Failed to assign role');
    });
  });

  describe('error handling', () => {
    it('should handle non-JSON error responses', async () => {
      const mockResponse = new Response('Internal Server Error', {
        status: 500,
        statusText: 'Internal Server Error',
      });
      vi.mocked(global.fetch).mockResolvedValueOnce(mockResponse);

      await expect(roleService.getUserRoles('tenant-1', 'user-1')).rejects.toThrow(
        'API Error: 500 Internal Server Error'
      );
    });

    it('should use auth token from localStorage', async () => {
      localStorage.setItem('auth_token', 'custom-token');
      const mockResponse = new Response(JSON.stringify([]), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
      });
      vi.mocked(global.fetch).mockResolvedValueOnce(mockResponse);

      await roleService.getUserRoles('tenant-1', 'user-1');

      expect(global.fetch).toHaveBeenCalledWith(
        expect.any(String),
        expect.objectContaining({
          headers: expect.objectContaining({
            'Authorization': 'Bearer custom-token',
          }),
        })
      );
    });

    it('should handle missing auth token', async () => {
      localStorage.removeItem('auth_token');
      const mockResponse = new Response(JSON.stringify([]), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
      });
      vi.mocked(global.fetch).mockResolvedValueOnce(mockResponse);

      await roleService.getUserRoles('tenant-1', 'user-1');

      expect(global.fetch).toHaveBeenCalledWith(
        expect.any(String),
        expect.objectContaining({
          headers: expect.objectContaining({
            'Authorization': '',
          }),
        })
      );
    });
  });
});
