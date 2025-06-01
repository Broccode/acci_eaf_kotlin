import {
  Box,
  ThemeProvider,
  Typography,
  createTheme
} from '@mui/material';
import type React from 'react';
import { useCallback, useEffect, useState } from 'react';
import {
  Admin,
  AppBar,
  Layout,
  Menu,
  Resource,
  TitlePortal
} from 'react-admin';
import { authProvider } from './features/auth';
import { TenantCreate, TenantEdit, TenantList, TenantShow } from './features/tenants';
import { UserCreate, UserEdit, UserList, UserShow } from './features/users';
import { Dashboard } from './pages/Dashboard';
import { LoginPage } from './pages/LoginPage';
import { dataProvider } from './services/dataProvider';

// Material-UI Theme für professionelles Aussehen
const theme = createTheme({
  palette: {
    primary: {
      main: '#1976d2',
    },
    secondary: {
      main: '#dc004e',
    },
  },
});

// Layout Props Interface
interface CustomLayoutProps {
  children: React.ReactNode;
}

// Custom AppBar mit korrekter React-Admin Struktur
const CustomAppBar = () => (
  <AppBar>
    <TitlePortal>
      <Typography variant="h4">ACCI EAF Control Plane</Typography>
    </TitlePortal>
  </AppBar>
);

// Custom Menu für Navigation
const CustomMenu = () => (
  <Menu>
    {/* Navigation Links für Tenant Management */}
    {/* Weitere Menüpunkte werden in zukünftigen Stories hinzugefügt: */}
    {/* - Benutzer */}
    {/* - Service Accounts */}
    {/* - Lizenzen */}
    {/* - Einstellungen */}
  </Menu>
);

// Custom Layout mit Responsiveness
const CustomLayout = (props: CustomLayoutProps) => (
  <Layout
    {...props}
    appBar={CustomAppBar}
    menu={CustomMenu}
    sx={{
      '& .RaLayout-content': {
        padding: { xs: 1, sm: 2, md: 3 },
      },
    }}
  />
);

// Dummy-Liste für Tenants (wird später durch echte Komponente ersetzt)
// const TenantList = () => <div>Tenant List - Coming Soon</div>;

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState<boolean | null>(null);

  // Funktion zum Prüfen des Authentication-Status
  const checkAuthStatus = useCallback(async () => {
    console.log('🔍 App: Checking authentication status...');
    try {
      await authProvider.checkAuth({});
      console.log('🎯 App: User is authenticated');
      setIsAuthenticated(true);
    } catch {
      console.log('🎯 App: User is not authenticated');
      setIsAuthenticated(false);
    }
  }, []);

  // Callback für erfolgreichen Login
  const handleLoginSuccess = () => {
    console.log('🎉 App: Login success callback received');
    // Auth-Status neu prüfen nach erfolgreichem Login
    checkAuthStatus();
  };

  useEffect(() => {
    // Prüfe Authentication-Status beim App-Start
    checkAuthStatus();
  }, [checkAuthStatus]);

  // Loading state während Authentication-Check
  if (isAuthenticated === null) {
    return (
      <ThemeProvider theme={theme}>
        <Box
          display="flex"
          justifyContent="center"
          alignItems="center"
          minHeight="100vh"
        >
          <Typography>Loading...</Typography>
        </Box>
      </ThemeProvider>
    );
  }

  // Wenn nicht authenticated, zeige Login-Seite mit Callback
  if (!isAuthenticated) {
    return (
      <ThemeProvider theme={theme}>
        <LoginPage onLoginSuccess={handleLoginSuccess} />
      </ThemeProvider>
    );
  }

  // Wenn authenticated, zeige die normale React-Admin App
  return (
    <ThemeProvider theme={theme}>
      <Admin
        title="ACCI EAF Control Plane"
        authProvider={authProvider}
        dataProvider={dataProvider}
        layout={CustomLayout}
        dashboard={Dashboard}
        disableTelemetry={true}
      >
        {/* Tenant Management Resource mit vollständigen CRUD-Operationen */}
        <Resource
          name="tenants"
          list={TenantList}
          create={TenantCreate}
          edit={TenantEdit}
          show={TenantShow}
          options={{ label: 'Tenants' }}
        />
        {/* User Management Resource mit vollständigen CRUD-Operationen */}
        <Resource
          name="users"
          list={UserList}
          create={UserCreate}
          edit={UserEdit}
          show={UserShow}
          options={{ label: 'Users' }}
        />
      </Admin>
    </ThemeProvider>
  );
}

export default App;
