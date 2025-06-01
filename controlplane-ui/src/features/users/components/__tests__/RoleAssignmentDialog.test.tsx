import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { vi } from 'vitest';
import { roleService } from '../../../services/roleService';
import { TestWrapper } from '../../../test-utils';
import type { Role } from '../../../types/role';
import { RoleAssignmentDialog } from '../RoleAssignmentDialog';

// Mock the services
vi.mock('../../../services/roleService');

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
  {
    id: 'role-3',
    name: 'Viewer',
    description: 'Read-only access',
    tenantId: 'test-tenant-id',
    permissionIds: ['perm-4'],
  },
];

const mockCurrentRoles: Role[] = [mockRoles[0]]; // User currently has System Administrator role

const defaultProps = {
  open: true,
  onClose: vi.fn(),
  userId: 'user-1',
  tenantId: 'test-tenant-id',
  currentRoles: mockCurrentRoles,
  onRolesUpdated: vi.fn(),
};

describe('RoleAssignmentDialog', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(roleService.getAvailableRoles).mockResolvedValue(mockRoles);
    vi.mocked(roleService.updateUserRoles).mockResolvedValue(undefined);
  });

  it('should not render when closed', () => {
    render(
      <TestWrapper>
        <RoleAssignmentDialog {...defaultProps} open={false} />
      </TestWrapper>
    );

    expect(screen.queryByText('Manage User Roles')).not.toBeInTheDocument();
  });

  it('should render dialog when open', () => {
    render(
      <TestWrapper>
        <RoleAssignmentDialog {...defaultProps} />
      </TestWrapper>
    );

    expect(screen.getByText('Manage User Roles')).toBeInTheDocument();
    expect(screen.getByText('Cancel')).toBeInTheDocument();
    expect(screen.getByText('Save Changes')).toBeInTheDocument();
  });

  it('should load available roles when opened', async () => {
    render(
      <TestWrapper>
        <RoleAssignmentDialog {...defaultProps} />
      </TestWrapper>
    );

    await waitFor(() => {
      expect(roleService.getAvailableRoles).toHaveBeenCalledWith('test-tenant-id');
    });
  });

  it('should show loading state while fetching roles', () => {
    render(
      <TestWrapper>
        <RoleAssignmentDialog {...defaultProps} />
      </TestWrapper>
    );

    expect(screen.getByRole('progressbar')).toBeInTheDocument();
  });

  it('should display available roles in autocomplete after loading', async () => {
    render(
      <TestWrapper>
        <RoleAssignmentDialog {...defaultProps} />
      </TestWrapper>
    );

    await waitFor(() => {
      expect(screen.queryByRole('progressbar')).not.toBeInTheDocument();
    });

    const input = screen.getByLabelText('Select Roles');
    fireEvent.click(input);

    await waitFor(() => {
      expect(screen.getByText('System Administrator')).toBeInTheDocument();
      expect(screen.getByText('Tenant Admin')).toBeInTheDocument();
      expect(screen.getByText('Viewer')).toBeInTheDocument();
    });
  });

  it('should pre-select current roles', async () => {
    render(
      <TestWrapper>
        <RoleAssignmentDialog {...defaultProps} />
      </TestWrapper>
    );

    await waitFor(() => {
      expect(screen.queryByRole('progressbar')).not.toBeInTheDocument();
    });

    // Check that the current role is displayed as a chip
    expect(screen.getByText('System Administrator')).toBeInTheDocument();
  });

  it('should show confirmation dialog when saving', async () => {
    render(
      <TestWrapper>
        <RoleAssignmentDialog {...defaultProps} />
      </TestWrapper>
    );

    await waitFor(() => {
      expect(screen.queryByRole('progressbar')).not.toBeInTheDocument();
    });

    const saveButton = screen.getByText('Save Changes');
    fireEvent.click(saveButton);

    expect(screen.getByText('Confirm Role Changes')).toBeInTheDocument();
    expect(screen.getByText('Are you sure you want to update the user\'s roles?')).toBeInTheDocument();
  });

  it('should call updateUserRoles when confirmed', async () => {
    render(
      <TestWrapper>
        <RoleAssignmentDialog {...defaultProps} />
      </TestWrapper>
    );

    await waitFor(() => {
      expect(screen.queryByRole('progressbar')).not.toBeInTheDocument();
    });

    // Save changes
    fireEvent.click(screen.getByText('Save Changes'));

    // Confirm
    fireEvent.click(screen.getByText('Update Roles'));

    await waitFor(() => {
      expect(roleService.updateUserRoles).toHaveBeenCalledWith(
        'test-tenant-id',
        'user-1',
        ['role-1'], // current role IDs
        ['role-1']  // selected role IDs (unchanged in this test)
      );
      expect(defaultProps.onRolesUpdated).toHaveBeenCalled();
    });
  });

  it('should handle role addition and removal', async () => {
    render(
      <TestWrapper>
        <RoleAssignmentDialog {...defaultProps} />
      </TestWrapper>
    );

    await waitFor(() => {
      expect(screen.queryByRole('progressbar')).not.toBeInTheDocument();
    });

    // Open autocomplete
    const input = screen.getByLabelText('Select Roles');
    fireEvent.click(input);

    // Add a new role
    await waitFor(() => {
      const tenantAdminOption = screen.getByText('Tenant Admin');
      fireEvent.click(tenantAdminOption);
    });

    // Save changes
    fireEvent.click(screen.getByText('Save Changes'));

    // Confirm
    fireEvent.click(screen.getByText('Update Roles'));

    await waitFor(() => {
      expect(roleService.updateUserRoles).toHaveBeenCalledWith(
        'test-tenant-id',
        'user-1',
        ['role-1'], // current role IDs
        expect.arrayContaining(['role-1', 'role-2']) // should include both roles
      );
    });
  });

  it('should handle API errors gracefully', async () => {
    const consoleError = vi.spyOn(console, 'error').mockImplementation(() => {});
    vi.mocked(roleService.updateUserRoles).mockRejectedValue(new Error('API Error'));

    render(
      <TestWrapper>
        <RoleAssignmentDialog {...defaultProps} />
      </TestWrapper>
    );

    await waitFor(() => {
      expect(screen.queryByRole('progressbar')).not.toBeInTheDocument();
    });

    // Save and confirm
    fireEvent.click(screen.getByText('Save Changes'));
    fireEvent.click(screen.getByText('Update Roles'));

    await waitFor(() => {
      expect(consoleError).toHaveBeenCalledWith('Error updating user roles:', expect.any(Error));
    });

    consoleError.mockRestore();
  });

  it('should close dialog when cancel is clicked', () => {
    render(
      <TestWrapper>
        <RoleAssignmentDialog {...defaultProps} />
      </TestWrapper>
    );

    fireEvent.click(screen.getByText('Cancel'));
    expect(defaultProps.onClose).toHaveBeenCalled();
  });
});
