import { PlayArrow, Stop } from '@mui/icons-material';
import { Box, Chip, Tooltip, Typography } from '@mui/material';
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
import type { Tenant } from '../../../types/tenant';

/**
 * Tenant status field with color-coded chip
 * Following React-Admin pattern of focused field components
 */
const TenantStatusField = () => {
  const record = useRecordContext<Tenant>();
  if (!record?.status) return null;

  const statusConfig: Record<string, { color: 'success' | 'warning' | 'error' | 'info' | 'default'; label: string }> = {
    ACTIVE: { color: 'success', label: 'Active' },
    INACTIVE: { color: 'warning', label: 'Inactive' },
    SUSPENDED: { color: 'error', label: 'Suspended' },
    PENDING_VERIFICATION: { color: 'info', label: 'Pending' },
    ARCHIVED: { color: 'default', label: 'Archived' },
  };

  const config = statusConfig[record.status] || { color: 'default', label: record.status };

  return (
    <Chip
      label={config.label}
      color={config.color}
      size="small"
      variant="outlined"
    />
  );
};

/**
 * Tenant action buttons for activation/deactivation
 * Separated for better composition
 */
const TenantQuickActions = () => {
  const record = useRecordContext<Tenant>();
  const notify = useNotify();
  const refresh = useRefresh();
  const [update, { isLoading }] = useUpdate();
  const [confirmAction, setConfirmAction] = useState<'activate' | 'deactivate' | null>(null);

  if (!record) return null;

  const handleStatusUpdate = async (newStatus: 'ACTIVE' | 'INACTIVE') => {
    try {
      await update('tenants', {
        id: record.id,
        data: { status: newStatus },
        previousData: record,
      });
      notify(`Tenant ${newStatus === 'ACTIVE' ? 'activated' : 'deactivated'} successfully`, { type: 'success' });
      refresh();
    } catch (error) {
      console.error('Error updating tenant status:', error);
      notify('Error updating tenant status', { type: 'error' });
    } finally {
      setConfirmAction(null);
    }
  };

  const showActivateButton = record.status === 'INACTIVE';
  const showDeactivateButton = record.status === 'ACTIVE';

  return (
    <>
      {showActivateButton && (
        <Tooltip title="Activate Tenant">
          <Button
            onClick={() => setConfirmAction('activate')}
            color="success"
            size="small"
            disabled={isLoading}
          >
            <PlayArrow />
          </Button>
        </Tooltip>
      )}

      {showDeactivateButton && (
        <Tooltip title="Deactivate Tenant">
          <Button
            onClick={() => setConfirmAction('deactivate')}
            color="warning"
            size="small"
            disabled={isLoading}
          >
            <Stop />
          </Button>
        </Tooltip>
      )}

      <Confirm
        isOpen={confirmAction === 'activate'}
        title="Activate Tenant"
        content={`Are you sure you want to activate tenant "${record.name}"?`}
        onConfirm={() => handleStatusUpdate('ACTIVE')}
        onClose={() => setConfirmAction(null)}
      />

      <Confirm
        isOpen={confirmAction === 'deactivate'}
        title="Deactivate Tenant"
        content={`Are you sure you want to deactivate tenant "${record.name}"? Users will not be able to access this tenant.`}
        onConfirm={() => handleStatusUpdate('INACTIVE')}
        onClose={() => setConfirmAction(null)}
      />
    </>
  );
};

/**
 * Tenant row actions
 * Using composition pattern for better organization
 */
const TenantRowActions = () => (
  <>
    <ShowButton />
    <EditButton />
    <TenantQuickActions />
    <DeleteButton
      mutationMode="pessimistic"
      confirmTitle="Delete Tenant"
      confirmContent="Are you sure you want to delete this tenant? This action cannot be undone."
    />
  </>
);

/**
 * List actions toolbar
 * Clean separation of concerns
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
 * Follows React-Admin patterns
 */
const TenantBulkActions = () => (
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
 * Empty state component
 * Better UX when no tenants exist
 */
const EmptyTenantList = () => (
  <Box sx={{ textAlign: 'center', mt: 4 }}>
    <Typography variant="h6" gutterBottom>
      No tenants found
    </Typography>
    <Typography variant="body2" color="text.secondary" paragraph>
      Create your first tenant to get started with the platform.
    </Typography>
    <CreateButton variant="contained" label="Create First Tenant" />
  </Box>
);

/**
 * Search filters for tenant list
 * Declarative filter definition
 */
const tenantFilters = [
  <SearchInput
    key="search"
    source="q"
    placeholder="Search tenants..."
    alwaysOn
  />,
];

/**
 * Main tenant list component
 * Following React-Admin best practices:
 * - Smart component handling data fetching
 * - Composition over configuration
 * - Clean separation of concerns
 */
export const TenantList = () => (
  <List
    title="Tenant Management"
    filters={tenantFilters}
    actions={<TenantListActions />}
    sort={{ field: 'createdAt', order: 'DESC' }}
    perPage={25}
    empty={<EmptyTenantList />}
  >
    <Datagrid
      bulkActionButtons={<TenantBulkActions />}
      rowClick="show"
    >
      <TextField source="id" label="Tenant ID" sortable={false} />
      <TextField source="name" />
      <TextField source="description" />
      <TenantStatusField />
      <DateField source="createdAt" label="Created" showTime />
      <DateField source="updatedAt" label="Updated" showTime />
      <TenantRowActions />
    </Datagrid>
  </List>
);
