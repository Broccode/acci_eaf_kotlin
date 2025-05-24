import { Info, PlayArrow, Stop } from '@mui/icons-material';
import { Box, Card, CardContent, Chip, Tooltip, Typography } from '@mui/material';
import { useState } from 'react';
import {
  Button,
  Confirm,
  DateField,
  DeleteButton,
  EditButton,
  ListButton,
  Show,
  SimpleShowLayout,
  TextField,
  TopToolbar,
  useNotify,
  useRecordContext,
  useRefresh,
  useUpdate,
} from 'react-admin';
import type { Tenant } from '../../types/tenant';

/**
 * Status display component with color coding and description
 */
const TenantStatusDisplay = () => {
  const record = useRecordContext<Tenant>();
  if (!record) return null;

  const getStatusInfo = (status: string) => {
    switch (status) {
      case 'ACTIVE':
        return {
          color: 'success' as const,
          description: 'Tenant is fully operational and can be used normally',
        };
      case 'INACTIVE':
        return {
          color: 'warning' as const,
          description: 'Tenant is temporarily deactivated but can be reactivated',
        };
      case 'SUSPENDED':
        return {
          color: 'error' as const,
          description: 'Tenant is suspended due to policy violations or other issues',
        };
      case 'PENDING_VERIFICATION':
        return {
          color: 'info' as const,
          description: 'Tenant exists but is not yet ready for use',
        };
      case 'ARCHIVED':
        return {
          color: 'default' as const,
          description: 'Tenant has been archived and cannot be reactivated',
        };
      default:
        return {
          color: 'default' as const,
          description: 'Unknown status',
        };
    }
  };

  const statusInfo = getStatusInfo(record.status);

  return (
    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
      <Chip
        label={record.status}
        color={statusInfo.color}
        size="medium"
        variant="outlined"
      />
      <Tooltip title={statusInfo.description}>
        <Info color="action" fontSize="small" />
      </Tooltip>
    </Box>
  );
};

/**
 * Action buttons for tenant management
 */
const TenantShowActions = () => {
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
    <TopToolbar>
      <ListButton />
      <EditButton />

      {record.status === 'INACTIVE' && (
        <Button
          onClick={() => setShowConfirm('activate')}
          color="success"
          variant="contained"
          startIcon={<PlayArrow />}
        >
          Activate
        </Button>
      )}

      {record.status === 'ACTIVE' && (
        <Button
          onClick={() => setShowConfirm('deactivate')}
          color="warning"
          variant="contained"
          startIcon={<Stop />}
        >
          Deactivate
        </Button>
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
    </TopToolbar>
  );
};

/**
 * Main tenant detail view component
 */
export const TenantShow = () => {
  const record = useRecordContext<Tenant>();

  return (
    <Show title={record ? `Tenant: ${record.name}` : 'Tenant Details'} actions={<TenantShowActions />}>
      <SimpleShowLayout>
        <Box sx={{ display: 'flex', flexDirection: { xs: 'column', md: 'row' }, gap: 3 }}>
          <Box sx={{ flex: 2 }}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Basic Information
                </Typography>

                <Box sx={{ mb: 2 }}>
                  <TextField source="id" label="Tenant ID" />
                </Box>

                <Box sx={{ mb: 2 }}>
                  <TextField source="name" label="Name" />
                </Box>

                <Box sx={{ mb: 2 }}>
                  <TextField source="description" label="Description" />
                </Box>

                <Box sx={{ mb: 2 }}>
                  <Typography variant="subtitle2" color="text.secondary" gutterBottom>
                    Status
                  </Typography>
                  <TenantStatusDisplay />
                </Box>
              </CardContent>
            </Card>
          </Box>

          <Box sx={{ flex: 1 }}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Timestamps
                </Typography>

                <Box sx={{ mb: 2 }}>
                  <DateField source="createdAt" label="Created" showTime />
                </Box>

                <Box sx={{ mb: 2 }}>
                  <DateField source="updatedAt" label="Last Updated" showTime />
                </Box>
              </CardContent>
            </Card>

            <Card sx={{ mt: 2 }}>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Quick Actions
                </Typography>

                <Typography variant="body2" color="text.secondary">
                  Use the action buttons in the toolbar above to manage this tenant.
                </Typography>
              </CardContent>
            </Card>
          </Box>
        </Box>
      </SimpleShowLayout>
    </Show>
  );
};
