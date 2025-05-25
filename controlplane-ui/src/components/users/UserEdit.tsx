import { Box, Card, CardContent, Typography } from '@mui/material';
import {
  Edit,
  SaveButton,
  SelectInput,
  SimpleForm,
  TextInput,
  Toolbar,
  email,
  required,
} from 'react-admin';
import { useParams } from 'react-router-dom';

/**
 * Custom toolbar for user editing
 */
const UserEditToolbar = () => (
  <Toolbar>
    <SaveButton />
  </Toolbar>
);

/**
 * User editing component
 */
export const UserEdit = () => {
  const { tenantId } = useParams();

  if (!tenantId) {
    return (
      <Typography variant="h6" color="error">
        Error: Tenant ID is required
      </Typography>
    );
  }

  return (
    <Edit
      title="Edit User"
      redirect={`/tenants/${tenantId}/users`}
      mutationOptions={{
        meta: { tenantId },
      }}
    >
      <SimpleForm toolbar={<UserEditToolbar />}>
        <Box sx={{ maxWidth: 600, width: '100%' }}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                User Information
              </Typography>

              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                <TextInput
                  source="username"
                  label="Username"
                  disabled
                  fullWidth
                  helperText="Username cannot be changed after creation"
                />

                <TextInput
                  source="email"
                  label="Email Address"
                  type="email"
                  validate={[required(), email()]}
                  fullWidth
                />

                <SelectInput
                  source="status"
                  label="Status"
                  choices={[
                    { id: 'ACTIVE', name: 'Active' },
                    { id: 'LOCKED', name: 'Locked' },
                    { id: 'DISABLED', name: 'Disabled' },
                  ]}
                  fullWidth
                  helperText="Change user status to control access"
                />
              </Box>
            </CardContent>
          </Card>
        </Box>
      </SimpleForm>
    </Edit>
  );
};
