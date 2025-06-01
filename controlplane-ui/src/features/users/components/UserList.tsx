import { AdminPanelSettings, PersonAdd } from '@mui/icons-material';
import { Box, Chip, Tooltip, Typography } from '@mui/material';
import {
  CreateButton,
  Datagrid,
  DateField,
  EmailField,
  FilterButton,
  List,
  SearchInput,
  SelectInput,
  TextField,
  TextInput,
  TopToolbar,
  useRecordContext,
} from 'react-admin';
import { useParams } from 'react-router-dom';
import type { User, UserStatus } from '../../../types/user';

/**
 * User status field with color coding
 * Follows React-Admin pattern of focused field components
 */
const UserStatusField = () => {
  const record = useRecordContext<User>();
  if (!record?.status) return null;

  const statusConfig: Record<UserStatus, { color: 'success' | 'warning' | 'error'; label: string }> = {
    ACTIVE: { color: 'success', label: 'Active' },
    LOCKED: { color: 'warning', label: 'Locked' },
    DISABLED: { color: 'error', label: 'Disabled' },
  };

  const config = statusConfig[record.status] || { color: 'default' as const, label: record.status };

  return (
    <Chip
      label={config.label}
      color={config.color as 'success' | 'warning' | 'error' | 'default'}
      size="small"
      variant="outlined"
    />
  );
};

/**
 * User role field - placeholder for future role count display
 * Uses composition pattern for better maintainability
 */
const UserRoleField = () => {
  const record = useRecordContext<User>();
  if (!record) return null;

  // TODO: Replace with actual role count when API supports it
  return (
    <Tooltip title="View user details to see assigned roles">
      <Chip
        icon={<AdminPanelSettings />}
        label="View"
        size="small"
        variant="outlined"
        color="primary"
        sx={{ cursor: 'pointer' }}
      />
    </Tooltip>
  );
};

/**
 * User list actions toolbar
 * Separated for better composition and reusability
 */
const UserListActions = () => {
  const { tenantId } = useParams();

  return (
    <TopToolbar>
      <FilterButton />
      <CreateButton
        icon={<PersonAdd />}
        label="Add User"
        variant="contained"
        to={`/tenants/${tenantId}/users/create`}
      />
    </TopToolbar>
  );
};

/**
 * User list filters
 * Follows React-Admin pattern of declarative filters
 */
const userFilters = [
  <SearchInput
    source="q"
    placeholder="Search users..."
    alwaysOn
    key="search"
  />,
  <SelectInput
    source="status"
    choices={[
      { id: 'ACTIVE', name: 'Active' },
      { id: 'LOCKED', name: 'Locked' },
      { id: 'DISABLED', name: 'Disabled' },
    ]}
    emptyText="All statuses"
    key="status"
  />,
  <TextInput
    source="email"
    placeholder="Email contains..."
    key="email"
  />,
];

/**
 * Empty state component for better UX
 */
const EmptyUserList = () => (
  <Box sx={{ textAlign: 'center', mt: 4 }}>
    <Typography variant="h6" gutterBottom>
      No users found
    </Typography>
    <Typography variant="body2" color="text.secondary">
      Add your first user to get started
    </Typography>
  </Box>
);

/**
 * User list component following React-Admin best practices
 * - Smart component that handles data fetching
 * - Composition over configuration
 * - Declarative field definitions
 */
export const UserList = () => {
  const { tenantId } = useParams();

  if (!tenantId) {
    return (
      <Typography variant="h6" color="error" sx={{ p: 2 }}>
        Error: Tenant ID is required
      </Typography>
    );
  }

  return (
    <List
      title="Tenant Users"
      filters={userFilters}
      actions={<UserListActions />}
      queryOptions={{
        meta: { tenantId },
      }}
      perPage={25}
      sort={{ field: 'username', order: 'ASC' }}
      empty={<EmptyUserList />}
    >
      <Datagrid
        rowClick="show"
        bulkActionButtons={false}
        sx={{
          '& .column-status': { width: '120px' },
          '& .column-email': { minWidth: '200px' },
          '& .column-roles': { width: '100px' },
        }}
      >
        <TextField source="username" />
        <EmailField source="email" />
        <UserStatusField />
        <UserRoleField />
        <DateField source="createdAt" label="Created" showTime={false} />
        <DateField source="updatedAt" label="Updated" showTime={false} />
      </Datagrid>
    </List>
  );
};
