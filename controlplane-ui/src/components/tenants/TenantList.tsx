import { PlayArrow, Stop } from '@mui/icons-material';
import { Chip, Tooltip } from '@mui/material';
import { useState } from 'react';
import {
  BulkDeleteButton,
  BulkExportButton,
  Button,
  Confirm,
  CreateButton,
  Datagrid,
  DateField,
  DeleteButton,
  EditButton,
  ExportButton,
  FilterButton,
  List,
  SearchInput,
  SelectColumnsButton,
  ShowButton,
  TextField,
  TopToolbar,
  useNotify,
  useRecordContext,
  useRefresh,
  useUpdate,
} from 'react-admin';
import type { Tenant } from '../../types/tenant';

/**
 * Status chip component with color coding
 */
const TenantStatusField = () => {
  const record = useRecordContext<Tenant>();
  if (!record) return null;

  const getStatusColor = (status: string): 'success' | 'warning' | 'error' | 'info' | 'default' => {
    switch (status) {
      case 'ACTIVE':
        return 'success';
      case 'INACTIVE':
        return 'warning';
      case 'SUSPENDED':
        return 'error';
      case 'PENDING_VERIFICATION':
        return 'info';
      case 'ARCHIVED':
        return 'default';
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
 * Action buttons for tenant activation/deactivation
 */
const TenantActionButtons = () => {
  const record = useRecordContext<Tenant>();
  const notify = useNotify();
  const refresh = useRefresh();
  const [update] = useUpdate();
  const [showConfirm, setShowConfirm] = useState<'activate' | 'deactivate' | null>(null);

  if (!record) return null;

  const handleActivate = async () => {
    try {
      await update('tenants', {
        id: record.id,
        data: { ...record, status: 'ACTIVE' },
        previousData: record,
      });
      notify('Tenant activated successfully', { type: 'success' });
      refresh();
    } catch (error) {
      console.error('Error activating tenant:', error);
      notify('Error activating tenant', { type: 'error' });
    }
    setShowConfirm(null);
  };

  const handleDeactivate = async () => {
    try {
      await update('tenants', {
        id: record.id,
        data: { ...record, status: 'INACTIVE' },
        previousData: record,
      });
      notify('Tenant deactivated successfully', { type: 'success' });
      refresh();
    } catch (error) {
      console.error('Error deactivating tenant:', error);
      notify('Error deactivating tenant', { type: 'error' });
    }
    setShowConfirm(null);
  };

  return (
    <>
      <ShowButton />
      <EditButton />

      {record.status === 'INACTIVE' && (
        <Tooltip title="Activate Tenant">
          <Button
            onClick={() => setShowConfirm('activate')}
            color="success"
            size="small"
          >
            <PlayArrow />
          </Button>
        </Tooltip>
      )}

      {record.status === 'ACTIVE' && (
        <Tooltip title="Deactivate Tenant">
          <Button
            onClick={() => setShowConfirm('deactivate')}
            color="warning"
            size="small"
          >
            <Stop />
          </Button>
        </Tooltip>
      )}

      <DeleteButton
        mutationMode="pessimistic"
        confirmTitle={`Delete tenant "${record.name}"`}
        confirmContent="Are you sure you want to delete this tenant? This action cannot be undone."
      />

      {/* Confirmation dialogs */}
      <Confirm
        isOpen={showConfirm === 'activate'}
        title="Activate Tenant"
        content={`Are you sure you want to activate tenant "${record.name}"?`}
        onConfirm={handleActivate}
        onClose={() => setShowConfirm(null)}
      />

      <Confirm
        isOpen={showConfirm === 'deactivate'}
        title="Deactivate Tenant"
        content={`Are you sure you want to deactivate tenant "${record.name}"? Users will not be able to access this tenant.`}
        onConfirm={handleDeactivate}
        onClose={() => setShowConfirm(null)}
      />
    </>
  );
};

/**
 * Custom toolbar with create and export buttons
 */
const TenantListActions = () => (
  <TopToolbar>
    <FilterButton />
    <CreateButton />
    <ExportButton />
    <SelectColumnsButton />
  </TopToolbar>
);

/**
 * Bulk actions for selected tenants
 */
const TenantBulkActionButtons = () => (
  <>
    <BulkExportButton />
    <BulkDeleteButton
      mutationMode="pessimistic"
      confirmTitle="Delete selected tenants"
      confirmContent="Are you sure you want to delete the selected tenants? This action cannot be undone."
    />
  </>
);

/**
 * Search filters for the tenant list
 */
const tenantFilters = [
  <SearchInput key="search" source="q" placeholder="Search tenants..." alwaysOn />,
];

/**
 * Main tenant list component
 */
export const TenantList = () => (
  <List
    title="Tenant Management"
    filters={tenantFilters}
    actions={<TenantListActions />}
    sort={{ field: 'createdAt', order: 'DESC' }}
    perPage={25}
  >
    <Datagrid
      bulkActionButtons={<TenantBulkActionButtons />}
      rowClick="show"
    >
      <TextField source="id" label="Tenant ID" sortable={false} />
      <TextField source="name" label="Name" />
      <TextField source="description" label="Description" />
      <TenantStatusField />
      <DateField source="createdAt" label="Created" showTime />
      <DateField source="updatedAt" label="Last Updated" showTime />
      <TenantActionButtons />
    </Datagrid>
  </List>
);
