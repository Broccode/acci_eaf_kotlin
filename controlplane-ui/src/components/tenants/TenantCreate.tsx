import { Box, Typography } from '@mui/material';
import {
  Create,
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
import type { CreateTenantRequest } from '../../types/tenant';

/**
 * Custom toolbar with save button
 */
const TenantCreateToolbar = () => (
  <Toolbar>
    <SaveButton />
  </Toolbar>
);

/**
 * Validation rules for tenant name based on documentation
 */
const validateTenantName = [
  required('Tenant name is required'),
  minLength(3, 'Tenant name must be at least 3 characters'),
  maxLength(100, 'Tenant name must not exceed 100 characters'),
  regex(
    /^[a-zA-Z0-9-]+$/,
    'Tenant name can only contain letters, numbers, and hyphens'
  ),
  regex(
    /^[a-zA-Z0-9].*[a-zA-Z0-9]$|^[a-zA-Z0-9]$/,
    'Tenant name cannot start or end with hyphens'
  ),
];

/**
 * Validation rules for description
 */
const validateDescription = [
  maxLength(1024, 'Description must not exceed 1024 characters'),
];

/**
 * Status options for tenant creation
 */
const statusChoices = [
  { id: 'PENDING_VERIFICATION', name: 'Pending Verification' },
  { id: 'ACTIVE', name: 'Active' },
  { id: 'INACTIVE', name: 'Inactive' },
];

/**
 * Transform function to prepare data for API
 */
const transform = (data: CreateTenantRequest): CreateTenantRequest => ({
  ...data,
  // Set default status if not provided
  status: data.status || 'PENDING_VERIFICATION',
});

/**
 * Main tenant creation component
 */
export const TenantCreate = () => {
  const notify = useNotify();
  const redirect = useRedirect();

  const onSuccess = () => {
    notify('Tenant created successfully', { type: 'success' });
    redirect('list', 'tenants');
  };

  const onError = (error: Error) => {
    console.error('Error creating tenant:', error);
    notify('Error creating tenant. Please check the form and try again.', { type: 'error' });
  };

  return (
    <Create
      title="Create New Tenant"
      mutationOptions={{ onSuccess, onError }}
      transform={transform}
    >
      <SimpleForm toolbar={<TenantCreateToolbar />}>
        <Box sx={{ maxWidth: 600, width: '100%' }}>
          <Typography variant="h6" gutterBottom>
            Tenant Information
          </Typography>

          <TextInput
            source="name"
            label="Tenant Name"
            validate={validateTenantName}
            helperText="Unique identifier for the tenant (3-100 characters, letters, numbers, and hyphens only)"
            fullWidth
            required
          />

          <TextInput
            source="description"
            label="Description"
            validate={validateDescription}
            helperText="Optional description of the tenant (max 1024 characters)"
            multiline
            rows={3}
            fullWidth
          />

          <SelectInput
            source="status"
            label="Initial Status"
            choices={statusChoices}
            defaultValue="PENDING_VERIFICATION"
            helperText="Initial status for the new tenant"
            fullWidth
          />

          <Box sx={{ mt: 2 }}>
            <Typography variant="body2" color="text.secondary">
              <strong>Note:</strong> After creation, the tenant will need to be configured
              with appropriate users, roles, and permissions before it can be used.
            </Typography>
          </Box>
        </Box>
      </SimpleForm>
    </Create>
  );
};
