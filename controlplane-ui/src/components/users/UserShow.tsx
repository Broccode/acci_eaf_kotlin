import { Block, Lock, LockOpen, VpnKey } from '@mui/icons-material';
import { Box, Card, CardContent, Chip, Tooltip, Typography } from '@mui/material';
import { useState } from 'react';
import {
  Button,
  Confirm,
  DateField,
  DeleteButton,
  EditButton,
  EmailField,
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
import { useParams } from 'react-router-dom';
import { resetUserPassword } from '../../services/userDataProvider';
import type { User, UserStatus } from '../../types/user';

/**
 * Status display component with color coding and description
 */
const UserStatusDisplay = () => {
  const record = useRecordContext<User>();
  if (!record) return null;

  const getStatusInfo = (status: UserStatus) => {
    switch (status) {
      case 'ACTIVE':
        return {
          color: 'success' as const,
          description: 'User is active and can access the system',
        };
      case 'LOCKED':
        return {
          color: 'warning' as const,
          description: 'User account is temporarily locked',
        };
      case 'DISABLED':
        return {
          color: 'error' as const,
          description: 'User account is disabled and cannot access the system',
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
        <VpnKey color="action" fontSize="small" />
      </Tooltip>
    </Box>
  );
};

/**
 * Password reset dialog component
 */
const PasswordResetDialog = ({
  open,
  onClose,
  onConfirm
}: {
  open: boolean;
  onClose: () => void;
  onConfirm: (password: string) => void;
}) => {
  const [newPassword, setNewPassword] = useState('');

  const handleConfirm = () => {
    if (newPassword.length >= 8) {
      onConfirm(newPassword);
      setNewPassword('');
    }
  };

  return (
    <Confirm
      isOpen={open}
      title="Reset User Password"
      content={
        <Box sx={{ mt: 2 }}>
          <Typography variant="body2" gutterBottom>
            Enter a new password for this user:
          </Typography>
          <input
            type="password"
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
            placeholder="New password (min 8 characters)"
            style={{
              width: '100%',
              padding: '8px',
              marginTop: '8px',
              border: '1px solid #ccc',
              borderRadius: '4px',
            }}
          />
        </Box>
      }
      onConfirm={handleConfirm}
      onClose={() => {
        setNewPassword('');
        onClose();
      }}
    />
  );
};

/**
 * Action buttons for user management
 */
const UserShowActions = () => {
  const record = useRecordContext<User>();
  const { tenantId } = useParams();
  const notify = useNotify();
  const refresh = useRefresh();
  const [update] = useUpdate();
  const [showConfirm, setShowConfirm] = useState<'activate' | 'lock' | 'disable' | 'password' | null>(null);

  if (!record || !tenantId) return null;

  const handleStatusChange = async (newStatus: UserStatus) => {
    try {
      await update('users', {
        id: record.id,
        data: { ...record, status: newStatus },
        previousData: record,
        meta: { tenantId },
      });
      notify(`User ${newStatus.toLowerCase()} successfully`, { type: 'success' });
      refresh();
    } catch (error) {
      console.error('Error updating user status:', error);
      notify('Error updating user status', { type: 'error' });
    }
    setShowConfirm(null);
  };

  const handlePasswordReset = async (newPassword: string) => {
    try {
      await resetUserPassword({
        userId: record.id,
        newPassword,
        tenantId,
      });
      notify('Password reset successfully', { type: 'success' });
    } catch (error) {
      console.error('Error resetting password:', error);
      notify('Error resetting password', { type: 'error' });
    }
    setShowConfirm(null);
  };

  return (
    <TopToolbar>
      <ListButton to={`/tenants/${tenantId}/users`} />
      <EditButton to={`/tenants/${tenantId}/users/${record.id}/edit`} />

      {record.status === 'DISABLED' && (
        <Button
          onClick={() => setShowConfirm('activate')}
          color="success"
          variant="contained"
          startIcon={<LockOpen />}
        >
          Activate
        </Button>
      )}

      {record.status === 'ACTIVE' && (
        <>
          <Button
            onClick={() => setShowConfirm('lock')}
            color="warning"
            variant="contained"
            startIcon={<Lock />}
          >
            Lock
          </Button>
          <Button
            onClick={() => setShowConfirm('disable')}
            color="error"
            variant="contained"
            startIcon={<Block />}
          >
            Disable
          </Button>
        </>
      )}

      {record.status === 'LOCKED' && (
        <Button
          onClick={() => setShowConfirm('activate')}
          color="success"
          variant="contained"
          startIcon={<LockOpen />}
        >
          Unlock
        </Button>
      )}

      <Button
        onClick={() => setShowConfirm('password')}
        color="primary"
        variant="outlined"
        startIcon={<VpnKey />}
      >
        Reset Password
      </Button>

      <DeleteButton
        mutationMode="pessimistic"
        confirmTitle={`Delete user "${record.username}"`}
        confirmContent="Are you sure you want to delete this user? This action cannot be undone."
        mutationOptions={{ meta: { tenantId } }}
      />

      {/* Confirmation dialogs */}
      <Confirm
        isOpen={showConfirm === 'activate'}
        title="Activate User"
        content={`Are you sure you want to activate user "${record.username}"?`}
        onConfirm={() => handleStatusChange('ACTIVE')}
        onClose={() => setShowConfirm(null)}
      />

      <Confirm
        isOpen={showConfirm === 'lock'}
        title="Lock User"
        content={`Are you sure you want to lock user "${record.username}"? They will not be able to log in.`}
        onConfirm={() => handleStatusChange('LOCKED')}
        onClose={() => setShowConfirm(null)}
      />

      <Confirm
        isOpen={showConfirm === 'disable'}
        title="Disable User"
        content={`Are you sure you want to disable user "${record.username}"? They will not be able to access the system.`}
        onConfirm={() => handleStatusChange('DISABLED')}
        onClose={() => setShowConfirm(null)}
      />

      <PasswordResetDialog
        open={showConfirm === 'password'}
        onClose={() => setShowConfirm(null)}
        onConfirm={handlePasswordReset}
      />
    </TopToolbar>
  );
};

/**
 * Main user detail view component
 */
export const UserShow = () => {
  const record = useRecordContext<User>();

  return (
    <Show title={record ? `User: ${record.username}` : 'User Details'} actions={<UserShowActions />}>
      <SimpleShowLayout>
        <Box sx={{ display: 'flex', flexDirection: { xs: 'column', md: 'row' }, gap: 3 }}>
          <Box sx={{ flex: 2 }}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  User Information
                </Typography>

                <Box sx={{ mb: 2 }}>
                  <TextField source="id" label="User ID" />
                </Box>

                <Box sx={{ mb: 2 }}>
                  <TextField source="username" label="Username" />
                </Box>

                <Box sx={{ mb: 2 }}>
                  <EmailField source="email" label="Email Address" />
                </Box>

                <Box sx={{ mb: 2 }}>
                  <Typography variant="subtitle2" color="text.secondary" gutterBottom>
                    Status
                  </Typography>
                  <UserStatusDisplay />
                </Box>

                <Box sx={{ mb: 2 }}>
                  <TextField source="tenantId" label="Tenant ID" />
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
                  Use the action buttons in the toolbar above to manage this user.
                </Typography>
              </CardContent>
            </Card>
          </Box>
        </Box>
      </SimpleShowLayout>
    </Show>
  );
};
