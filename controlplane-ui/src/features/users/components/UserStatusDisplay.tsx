import { VpnKey } from '@mui/icons-material';
import { Box, Chip, Tooltip } from '@mui/material';
import type { UserStatus } from '../../../types/user';

interface UserStatusDisplayProps {
  status: UserStatus;
}

/**
 * Status display component with color coding and description
 * Reusable component for displaying user status consistently
 */
export const UserStatusDisplay = ({ status }: UserStatusDisplayProps) => {
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

  const statusInfo = getStatusInfo(status);

  return (
    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
      <Chip
        label={status}
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
