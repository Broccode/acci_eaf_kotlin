import { Box } from '@mui/material';
import {
  Edit,
  SaveButton,
  SelectInput,
  SimpleForm,
  TextInput,
  Toolbar,
  maxLength,
  minLength,
  regex,
  required,
  useNotify,
  useRedirect,
} from 'react-admin';

/**
 * Custom toolbar for tenant edit form
 */
const TenantEditToolbar = () => (
  <Toolbar>
    <SaveButton />
  </Toolbar>
);

/**
 * Validation rules for tenant name
 * Based on API documentation: 3-100 characters, alphanumeric + hyphens, no leading/trailing hyphens
 */
const validateTenantName = [
  required('Tenant name is required'),
  minLength(3, 'Tenant name must be at least 3 characters'),
  maxLength(100, 'Tenant name must not exceed 100 characters'),
  regex(
    /^[a-zA-Z0-9]([a-zA-Z0-9-]*[a-zA-Z0-9])?$/,
    'Tenant name must contain only alphanumeric characters and hyphens, and cannot start or end with a hyphen'
  ),
];

/**
 * Validation rules for description
 */
const validateDescription = [
  maxLength(1024, 'Description must not exceed 1024 characters'),
];

/**
 * Status options for tenant
 */
const statusChoices = [
  { id: 'PENDING_VERIFICATION', name: 'Pending Verification' },
  { id: 'ACTIVE', name: 'Active' },
  { id: 'INACTIVE', name: 'Inactive' },
  { id: 'SUSPENDED', name: 'Suspended' },
  { id: 'ARCHIVED', name: 'Archived' },
];

/**
 * TenantEdit component for editing existing tenants
 */
export const TenantEdit = () => {
  const notify = useNotify();
  const redirect = useRedirect();

  const onSuccess = () => {
    notify('Tenant updated successfully', { type: 'success' });
    redirect('show', 'tenants');
  };

  const onError = (error: Error | { message: string }) => {
    console.error('Error updating tenant:', error);
    notify('Error updating tenant. Please try again.', { type: 'error' });
  };

  return (
    <Edit
      mutationOptions={{ onSuccess, onError }}
      sx={{
        '& .RaEdit-main': {
          maxWidth: 600,
          margin: '0 auto',
        },
      }}
    >
      <SimpleForm toolbar={<TenantEditToolbar />}>
        <Box sx={{ width: '100%', maxWidth: 500 }}>
          {/* Tenant ID - Read-only display */}
          <TextInput
            source="id"
            label="Tenant ID"
            disabled
            fullWidth
            helperText="Tenant ID cannot be changed"
            sx={{ mb: 2 }}
          />

          {/* Tenant Name */}
          <TextInput
            source="name"
            label="Tenant Name"
            validate={validateTenantName}
            fullWidth
            required
            helperText="3-100 characters, alphanumeric and hyphens only, no leading/trailing hyphens"
            sx={{ mb: 2 }}
          />

          {/* Description */}
          <TextInput
            source="description"
            label="Description"
            validate={validateDescription}
            fullWidth
            multiline
            rows={3}
            helperText="Optional description (max 1024 characters)"
            sx={{ mb: 2 }}
          />

          {/* Status */}
          <SelectInput
            source="status"
            label="Status"
            choices={statusChoices}
            validate={required('Status is required')}
            fullWidth
            helperText="Current status of the tenant"
            sx={{ mb: 2 }}
          />
        </Box>
      </SimpleForm>
    </Edit>
  );
};
