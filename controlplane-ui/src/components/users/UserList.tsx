import { PersonAdd } from '@mui/icons-material';
import { Chip, Typography } from '@mui/material';
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
import type { User, UserStatus } from '../../types/user';

/**
 * User status display component with color coding
 */
const UserStatusField = () => {
  const record = useRecordContext<User>();
  if (!record) return null;

  const getStatusColor = (status: UserStatus) => {
    switch (status) {
      case 'ACTIVE':
        return 'success';
      case 'LOCKED':
        return 'warning';
      case 'DISABLED':
        return 'error';
      default:
        return 'default';
    }
  };

  return (
    <Chip
      label={record.status}
      color={getStatusColor(record.status)}
      size="small"
      variant="outlined"
    />
  );
};

/**
 * User list filters
 */
const userFilters = [
  <SearchInput source="q" placeholder="Search users..." alwaysOn key="search" />,
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
  <TextInput source="email" placeholder="Email contains..." key="email" />,
];

/**
 * User list actions
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
 * User list component
 */
export const UserList = () => {
  const { tenantId } = useParams();

  if (!tenantId) {
    return (
      <Typography variant="h6" color="error">
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
    >
      <Datagrid
        rowClick="show"
        bulkActionButtons={false}
        sx={{
          '& .column-status': {
            width: '120px',
          },
          '& .column-email': {
            minWidth: '200px',
          },
        }}
      >
        <TextField source="username" label="Username" />
        <EmailField source="email" label="Email" />
        <UserStatusField />
        <DateField source="createdAt" label="Created" showTime={false} />
        <DateField source="updatedAt" label="Last Updated" showTime={false} />
      </Datagrid>
    </List>
  );
};
