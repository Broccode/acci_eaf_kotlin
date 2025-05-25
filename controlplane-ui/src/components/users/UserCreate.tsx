import { Box, Card, CardContent, Typography } from '@mui/material';
import {
  Create,
  PasswordInput,
  SaveButton,
  SelectInput,
  SimpleForm,
  TextInput,
  Toolbar,
  email,
  minLength,
  regex,
  required,
} from 'react-admin';
import { useParams } from 'react-router-dom';

/**
 * Custom toolbar for user creation
 */
const UserCreateToolbar = () => (
  <Toolbar>
    <SaveButton />
  </Toolbar>
);

/**
 * Password strength validator
 * Ensures passwords meet minimum security requirements
 */
const passwordValidator = [
  required(),
  minLength(8, 'Password must be at least 8 characters'),
  regex(
    /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]/,
    'Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character'
  ),
];

/**
 * Username validator
 * Ensures usernames follow standard conventions
 */
const usernameValidator = [
  required(),
  minLength(3, 'Username must be at least 3 characters'),
  regex(/^[a-zA-Z0-9._-]+$/, 'Username can only contain letters, numbers, dots, underscores, and hyphens'),
];

/**
 * User creation component
 */
export const UserCreate = () => {
  const { tenantId } = useParams();

  if (!tenantId) {
    return (
      <Typography variant="h6" color="error">
        Error: Tenant ID is required
      </Typography>
    );
  }

  return (
    <Create
      title="Create New User"
      redirect={`/tenants/${tenantId}/users`}
      mutationOptions={{
        meta: { tenantId },
      }}
    >
      <SimpleForm toolbar={<UserCreateToolbar />}>
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
                  validate={usernameValidator}
                  fullWidth
                  helperText="Username must be unique within the tenant"
                />

                <TextInput
                  source="email"
                  label="Email Address"
                  type="email"
                  validate={[required(), email()]}
                  fullWidth
                />

                <PasswordInput
                  source="password"
                  label="Initial Password"
                  validate={passwordValidator}
                  fullWidth
                  helperText="Password must be at least 8 characters with uppercase, lowercase, number, and special character"
                />

                <SelectInput
                  source="status"
                  label="Initial Status"
                  choices={[
                    { id: 'ACTIVE', name: 'Active' },
                    { id: 'DISABLED', name: 'Disabled' },
                  ]}
                  defaultValue="ACTIVE"
                  fullWidth
                  helperText="User can be activated/deactivated later"
                />
              </Box>
            </CardContent>
          </Card>
        </Box>
      </SimpleForm>
    </Create>
  );
};
