import { AdminPanelSettings, BusinessCenter } from '@mui/icons-material';
import {
  Autocomplete,
  Box,
  Button,
  Chip,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  TextField,
  Typography,
} from '@mui/material';
import { useCallback, useEffect, useState } from 'react';
import { Confirm, useNotify } from 'react-admin';
import { roleService } from '../../../features/roles';
import type { Role } from '../../../types/role';
import { isSystemRole } from '../../../types/role';

interface RoleAssignmentDialogProps {
  open: boolean;
  onClose: () => void;
  userId: string;
  tenantId: string;
  currentRoles: Role[];
  onRolesUpdated: () => void;
}

/**
 * Dialog for managing role assignments for a user
 */
export const RoleAssignmentDialog = ({
  open,
  onClose,
  userId,
  tenantId,
  currentRoles,
  onRolesUpdated,
}: RoleAssignmentDialogProps) => {
  const notify = useNotify();

  const [availableRoles, setAvailableRoles] = useState<Role[]>([]);
  const [selectedRoles, setSelectedRoles] = useState<Role[]>([]);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [confirmOpen, setConfirmOpen] = useState(false);

  // Load available roles when dialog opens
  useEffect(() => {
    const loadAvailableRoles = async () => {
      if (!open || !tenantId) return;

      try {
        setLoading(true);
        const roles = await roleService.getAvailableRoles(tenantId);
        setAvailableRoles(roles);
      } catch (error) {
        console.error('Error loading available roles:', error);
        notify('Failed to load available roles', { type: 'error' });
      } finally {
        setLoading(false);
      }
    };

    loadAvailableRoles();
  }, [open, tenantId, notify]);

  // Set selected roles when current roles change
  useEffect(() => {
    setSelectedRoles(currentRoles);
  }, [currentRoles]);

  const handleSave = useCallback(() => {
    setConfirmOpen(true);
  }, []);

  const handleConfirmSave = useCallback(async () => {
    try {
      setSaving(true);

      const currentRoleIds = currentRoles.map(role => role.id);
      const selectedRoleIds = selectedRoles.map(role => role.id);

      await roleService.updateUserRoles(
        tenantId,
        userId,
        currentRoleIds,
        selectedRoleIds
      );

      onRolesUpdated();
    } catch (error) {
      console.error('Error updating user roles:', error);
      notify('Failed to update user roles', { type: 'error' });
    } finally {
      setSaving(false);
      setConfirmOpen(false);
    }
  }, [tenantId, userId, currentRoles, selectedRoles, onRolesUpdated, notify]);

  const getRoleOptionLabel = (role: Role) => {
    return role.name;
  };

  const getRoleOptionDescription = (role: Role) => {
    return `${role.description} (${isSystemRole(role) ? 'System-wide' : 'Tenant-specific'})`;
  };

  const isRoleEqual = (option: Role, value: Role) => {
    return option.id === value.id;
  };

  return (
    <>
      <Dialog
        open={open}
        onClose={onClose}
        maxWidth="sm"
        fullWidth
        aria-labelledby="role-assignment-dialog-title"
      >
        <DialogTitle id="role-assignment-dialog-title">
          Manage User Roles
        </DialogTitle>

        <DialogContent>
          <Box sx={{ mt: 2 }}>
            {loading ? (
              <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
                <CircularProgress />
              </Box>
            ) : (
              <Autocomplete
                multiple
                options={availableRoles}
                value={selectedRoles}
                onChange={(_, newValue) => setSelectedRoles(newValue)}
                getOptionLabel={getRoleOptionLabel}
                isOptionEqualToValue={isRoleEqual}
                renderInput={(params) => (
                  <TextField
                    {...params}
                    label="Select Roles"
                    placeholder="Choose roles to assign"
                    helperText="Select one or more roles to assign to this user"
                  />
                )}
                renderOption={(props, option) => (
                  <Box component="li" {...props}>
                    <Box sx={{ display: 'flex', alignItems: 'center', width: '100%' }}>
                      {isSystemRole(option) ? (
                        <AdminPanelSettings sx={{ mr: 1, color: 'primary.main' }} />
                      ) : (
                        <BusinessCenter sx={{ mr: 1, color: 'secondary.main' }} />
                      )}
                      <Box sx={{ flexGrow: 1 }}>
                        <Typography variant="body1">{option.name}</Typography>
                        <Typography variant="caption" color="text.secondary">
                          {getRoleOptionDescription(option)}
                        </Typography>
                      </Box>
                    </Box>
                  </Box>
                )}
                renderTags={(value, getTagProps) =>
                  value.map((option, index) => (
                    <Chip
                      {...getTagProps({ index })}
                      key={option.id}
                      label={option.name}
                      color={isSystemRole(option) ? 'primary' : 'secondary'}
                      variant="outlined"
                      icon={isSystemRole(option) ? <AdminPanelSettings /> : <BusinessCenter />}
                    />
                  ))
                }
                disabled={saving}
              />
            )}
          </Box>
        </DialogContent>

        <DialogActions>
          <Button onClick={onClose} disabled={saving}>
            Cancel
          </Button>
          <Button
            onClick={handleSave}
            color="primary"
            variant="contained"
            disabled={loading || saving}
          >
            Save Changes
          </Button>
        </DialogActions>
      </Dialog>

      <Confirm
        isOpen={confirmOpen}
        title="Confirm Role Changes"
        content={
          <Box>
            <Typography variant="body1" gutterBottom>
              Are you sure you want to update the user's roles?
            </Typography>
            <Typography variant="body2" color="text.secondary">
              This will immediately affect the user's permissions in the system.
            </Typography>
          </Box>
        }
        onConfirm={handleConfirmSave}
        onClose={() => setConfirmOpen(false)}
        confirm="Update Roles"
        cancel="Cancel"
      />
    </>
  );
};
