import { AdminPanelSettings, BusinessCenter, Edit } from '@mui/icons-material';
import {
  Box,
  Card,
  CardContent,
  Chip,
  CircularProgress,
  Stack,
  Tooltip,
  Typography,
} from '@mui/material';
import { useCallback, useEffect, useState } from 'react';
import {
  Button,
  useNotify,
  useRecordContext,
} from 'react-admin';
import { useParams } from 'react-router-dom';
import { roleService } from '../../../features/roles';
import type { Role } from '../../../types/role';
import { isSystemRole } from '../../../types/role';
import type { User } from '../../../types/user';
import { RoleAssignmentDialog } from './RoleAssignmentDialog';

/**
 * Component to display and manage roles for a user
 */
export const UserRoles = () => {
  const record = useRecordContext<User>();
  const { tenantId } = useParams();
  const notify = useNotify();

  const [roles, setRoles] = useState<Role[]>([]);
  const [loading, setLoading] = useState(true);
  const [dialogOpen, setDialogOpen] = useState(false);

  const loadUserRoles = useCallback(async () => {
    if (!record?.id || !tenantId) return;

    try {
      setLoading(true);
      const userRoles = await roleService.getUserRoles(tenantId, record.id);
      setRoles(userRoles);
    } catch (error) {
      console.error('Error loading user roles:', error);
      notify('Failed to load user roles', { type: 'error' });
    } finally {
      setLoading(false);
    }
  }, [record?.id, tenantId, notify]);

  useEffect(() => {
    loadUserRoles();
  }, [loadUserRoles]);

  const handleRolesUpdated = useCallback(() => {
    // Reload roles after successful update
    loadUserRoles();
    setDialogOpen(false);
    notify('User roles updated successfully', { type: 'success' });
  }, [loadUserRoles, notify]);

  if (!record || !tenantId) return null;

  return (
    <Card sx={{ mt: 2 }}>
      <CardContent>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Typography variant="h6" gutterBottom>
            Assigned Roles
          </Typography>
          <Button
            onClick={() => setDialogOpen(true)}
            color="primary"
            variant="outlined"
            startIcon={<Edit />}
            size="small"
          >
            Manage Roles
          </Button>
        </Box>

        {loading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', p: 2 }}>
            <CircularProgress size={24} />
          </Box>
        ) : roles.length === 0 ? (
          <Typography variant="body2" color="text.secondary">
            No roles assigned to this user
          </Typography>
        ) : (
          <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap>
            {roles.map((role) => (
              <Tooltip
                key={role.id}
                title={
                  <Box>
                    <Typography variant="body2">{role.description}</Typography>
                    <Typography variant="caption" sx={{ mt: 1, display: 'block' }}>
                      {isSystemRole(role) ? 'System-wide role' : 'Tenant-specific role'}
                    </Typography>
                  </Box>
                }
              >
                <Chip
                  label={role.name}
                  color={isSystemRole(role) ? 'primary' : 'secondary'}
                  variant="outlined"
                  icon={isSystemRole(role) ? <AdminPanelSettings /> : <BusinessCenter />}
                  sx={{ mb: 1 }}
                />
              </Tooltip>
            ))}
          </Stack>
        )}

        <RoleAssignmentDialog
          open={dialogOpen}
          onClose={() => setDialogOpen(false)}
          userId={record.id}
          tenantId={tenantId}
          currentRoles={roles}
          onRolesUpdated={handleRolesUpdated}
        />
      </CardContent>
    </Card>
  );
};
