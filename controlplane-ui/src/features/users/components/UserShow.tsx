import { Block, Lock, LockOpen, VpnKey } from '@mui/icons-material';
import { Box, Card, CardContent, Typography } from '@mui/material';
import { useState } from 'react';
import {
  Button,
  Confirm,
  DateField,
  DeleteButton,
  EditButton,
  EmailField,
  FunctionField,
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
import type { User, UserStatus } from '../../../types/user';
import { resetUserPassword } from '../services/userDataProvider';
import { UserRoles } from './UserRoles';
import { UserStatusDisplay } from './UserStatusDisplay';

/**
 * User actions toolbar component
 * Extracted for better composition and reusability
 */
const UserActions = () => {
  const record = useRecordContext<User>();
  const { tenantId } = useParams();
  const notify = useNotify();
  const refresh = useRefresh();
  const [update] = useUpdate();
  const [actionType, setActionType] = useState<'activate' | 'lock' | 'disable' | 'password' | null>(null);

  if (!record || !tenantId) return null;

  const handleStatusChange = async (newStatus: UserStatus) => {
    try {
      await update('users', {
        id: record.id,
        data: { status: newStatus },
        previousData: record,
        meta: { tenantId },
      });
      notify(`User ${newStatus.toLowerCase()} successfully`, { type: 'success' });
      refresh();
    } catch (error) {
      console.error('Error updating user status:', error);
      notify('Error updating user status', { type: 'error' });
    }
    setActionType(null);
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
    setActionType(null);
  };

  return (
    <>
      <TopToolbar>
        <ListButton to={`/tenants/${tenantId}/users`} />
        <EditButton to={`/tenants/${tenantId}/users/${record.id}/edit`} />
        <UserStatusActions record={record} onAction={setActionType} />
        <Button
          onClick={() => setActionType('password')}
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
      </TopToolbar>

      <UserActionDialogs
        actionType={actionType}
        onClose={() => setActionType(null)}
        onStatusChange={handleStatusChange}
        onPasswordReset={handlePasswordReset}
        username={record.username}
      />
    </>
  );
};

/**
 * User status action buttons
 * Separated for clarity and reusability
 */
const UserStatusActions = ({
  record,
  onAction
}: {
  record: User;
  onAction: (action: 'activate' | 'lock' | 'disable') => void;
}) => {
  switch (record.status) {
    case 'DISABLED':
      return (
        <Button
          onClick={() => onAction('activate')}
          color="success"
          variant="contained"
          startIcon={<LockOpen />}
        >
          Activate
        </Button>
      );
    case 'ACTIVE':
      return (
        <>
          <Button
            onClick={() => onAction('lock')}
            color="warning"
            variant="contained"
            startIcon={<Lock />}
          >
            Lock
          </Button>
          <Button
            onClick={() => onAction('disable')}
            color="error"
            variant="contained"
            startIcon={<Block />}
          >
            Disable
          </Button>
        </>
      );
    case 'LOCKED':
      return (
        <Button
          onClick={() => onAction('activate')}
          color="success"
          variant="contained"
          startIcon={<LockOpen />}
        >
          Unlock
        </Button>
      );
    default:
      return null;
  }
};

/**
 * Action confirmation dialogs
 * Consolidated for better organization
 */
const UserActionDialogs = ({
  actionType,
  onClose,
  onStatusChange,
  onPasswordReset,
  username,
}: {
  actionType: 'activate' | 'lock' | 'disable' | 'password' | null;
  onClose: () => void;
  onStatusChange: (status: UserStatus) => void;
  onPasswordReset: (password: string) => void;
  username: string;
}) => {
  const [newPassword, setNewPassword] = useState('');

  return (
    <>
      <Confirm
        isOpen={actionType === 'activate'}
        title="Activate User"
        content={`Are you sure you want to activate user "${username}"?`}
        onConfirm={() => onStatusChange('ACTIVE')}
        onClose={onClose}
      />

      <Confirm
        isOpen={actionType === 'lock'}
        title="Lock User"
        content={`Are you sure you want to lock user "${username}"? They will not be able to log in.`}
        onConfirm={() => onStatusChange('LOCKED')}
        onClose={onClose}
      />

      <Confirm
        isOpen={actionType === 'disable'}
        title="Disable User"
        content={`Are you sure you want to disable user "${username}"? They will not be able to access the system.`}
        onConfirm={() => onStatusChange('DISABLED')}
        onClose={onClose}
      />

      <Confirm
        isOpen={actionType === 'password'}
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
        onConfirm={() => {
          if (newPassword.length >= 8) {
            onPasswordReset(newPassword);
            setNewPassword('');
          }
        }}
        onClose={() => {
          setNewPassword('');
          onClose();
        }}
      />
    </>
  );
};

/**
 * User information card
 * Using React-Admin field components for consistency
 */
const UserInfoCard = () => (
  <Card>
    <CardContent>
      <Typography variant="h6" gutterBottom>
        User Information
      </Typography>

      <Box sx={{ '& > *': { mb: 2 } }}>
        <TextField source="id" label="User ID" />
        <TextField source="username" label="Username" />
        <EmailField source="email" label="Email Address" />
        <FunctionField
          label="Status"
          render={(record: User) => <UserStatusDisplay status={record.status} />}
        />
        <TextField source="tenantId" label="Tenant ID" />
      </Box>
    </CardContent>
  </Card>
);

/**
 * User timestamps card
 * Separated for better organization
 */
const UserTimestampsCard = () => (
  <Card>
    <CardContent>
      <Typography variant="h6" gutterBottom>
        Timestamps
      </Typography>

      <Box sx={{ '& > *': { mb: 2 } }}>
        <DateField source="createdAt" label="Created" showTime />
        <DateField source="updatedAt" label="Last Updated" showTime />
      </Box>
    </CardContent>
  </Card>
);

/**
 * Quick actions card
 * Simple informational component
 */
const QuickActionsCard = () => (
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
);

/**
 * Main user detail view component
 * Following React-Admin best practices:
 * - Uses composition for better organization
 * - Leverages React-Admin's smart components
 * - Separates concerns into focused components
 */
export const UserShow = () => {
  const record = useRecordContext<User>();

  return (
    <Show
      title={record ? `User: ${record.username}` : 'User Details'}
      actions={<UserActions />}
    >
      <SimpleShowLayout>
        <Box sx={{ display: 'flex', flexDirection: { xs: 'column', md: 'row' }, gap: 3 }}>
          <Box sx={{ flex: 2 }}>
            <UserInfoCard />
            <UserRoles />
          </Box>

          <Box sx={{ flex: 1 }}>
            <UserTimestampsCard />
            <QuickActionsCard />
          </Box>
        </Box>
      </SimpleShowLayout>
    </Show>
  );
};
