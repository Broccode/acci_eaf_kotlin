import {
  Assessment as AssessmentIcon,
  Business as BusinessIcon,
  People as PeopleIcon,
  Security as SecurityIcon
} from '@mui/icons-material';
import {
  Box,
  Card,
  CardContent,
  CardHeader,
  Paper,
  Typography,
  useMediaQuery,
  useTheme
} from '@mui/material';
import type React from 'react';

// Dashboard-Widget-Komponente
interface DashboardWidgetProps {
  title: string;
  value: string | number;
  icon: React.ReactNode;
  color: string;
}

const DashboardWidget: React.FC<DashboardWidgetProps> = ({
  title,
  value,
  icon,
  color
}) => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'));

  return (
    <Paper
      sx={{
        p: isMobile ? 1.5 : 2,
        display: 'flex',
        alignItems: 'center',
        borderLeft: `4px solid ${color}`,
        minHeight: isMobile ? 80 : 100,
        flex: 1,
        margin: 1,
      }}
    >
      <Box sx={{ mr: isMobile ? 1 : 2, color }}>
        {icon}
      </Box>
      <Box>
        <Typography
          variant={isMobile ? "h5" : "h4"}
          component="div"
          sx={{ fontSize: isMobile ? '1.25rem' : '2rem' }}
        >
          {value}
        </Typography>
        <Typography
          variant="body2"
          color="text.secondary"
          sx={{ fontSize: isMobile ? '0.75rem' : '0.875rem' }}
        >
          {title}
        </Typography>
      </Box>
    </Paper>
  );
};

// Haupt-Dashboard-Komponente
export const Dashboard = () => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'));

  return (
    <Box p={isMobile ? 2 : 3}>
      <Typography
        variant={isMobile ? "h5" : "h4"}
        gutterBottom
        sx={{ mb: isMobile ? 2 : 3 }}
      >
        Control Plane Dashboard
      </Typography>

      <Typography
        variant="body1"
        color="text.secondary"
        paragraph
        sx={{
          fontSize: isMobile ? '0.875rem' : '1rem',
          mb: isMobile ? 2 : 3
        }}
      >
        Willkommen im ACCI EAF Control Plane. Hier können Sie Tenants, Benutzer,
        Rollen und weitere Systemeinstellungen verwalten.
      </Typography>

      {/* Dashboard-Widgets */}
      <Box
        sx={{
          display: 'flex',
          flexWrap: 'wrap',
          gap: 1,
          mb: isMobile ? 3 : 4
        }}
      >
        <DashboardWidget
          title="Aktive Tenants"
          value="5"
          icon={<BusinessIcon fontSize={isMobile ? "medium" : "large"} />}
          color="#1976d2"
        />
        <DashboardWidget
          title="Benutzer"
          value="42"
          icon={<PeopleIcon fontSize={isMobile ? "medium" : "large"} />}
          color="#388e3c"
        />
        <DashboardWidget
          title="Service Accounts"
          value="12"
          icon={<SecurityIcon fontSize={isMobile ? "medium" : "large"} />}
          color="#f57c00"
        />
        <DashboardWidget
          title="Aktive Lizenzen"
          value="8"
          icon={<AssessmentIcon fontSize={isMobile ? "medium" : "large"} />}
          color="#7b1fa2"
        />
      </Box>

      {/* Übersichts-Karten */}
      <Box
        sx={{
          display: 'flex',
          flexDirection: { xs: 'column', md: 'row' },
          gap: isMobile ? 2 : 3
        }}
      >
        <Card sx={{ flex: 1 }}>
          <CardHeader
            title="Kürzliche Aktivitäten"
            titleTypographyProps={{
              variant: isMobile ? 'h6' : 'h5'
            }}
          />
          <CardContent>
            <Typography
              variant="body2"
              color="text.secondary"
              sx={{ fontSize: isMobile ? '0.75rem' : '0.875rem' }}
            >
              Hier werden später die neuesten Systemaktivitäten angezeigt:
            </Typography>
            <Box
              component="ul"
              sx={{
                mt: 1,
                pl: 2,
                '& li': {
                  fontSize: isMobile ? '0.75rem' : '0.875rem',
                  mb: 0.5
                }
              }}
            >
              <li>Neuer Tenant "Demo Corp" erstellt</li>
              <li>Benutzer "john.doe" aktiviert</li>
              <li>Service Account für "API Integration" generiert</li>
              <li>Lizenz für "ProductX" verlängert</li>
            </Box>
          </CardContent>
        </Card>

        <Card sx={{ flex: 1 }}>
          <CardHeader
            title="Systemstatus"
            titleTypographyProps={{
              variant: isMobile ? 'h6' : 'h5'
            }}
          />
          <CardContent>
            <Typography
              variant="body2"
              color="text.secondary"
              sx={{ fontSize: isMobile ? '0.75rem' : '0.875rem' }}
            >
              Systemkomponenten:
            </Typography>
            <Box sx={{ mt: 1 }}>
              <Box
                display="flex"
                justifyContent="space-between"
                mb={1}
                sx={{
                  flexDirection: isMobile ? 'column' : 'row',
                  alignItems: isMobile ? 'flex-start' : 'center'
                }}
              >
                <Typography
                  variant="body2"
                  sx={{ fontSize: isMobile ? '0.75rem' : '0.875rem' }}
                >
                  Database
                </Typography>
                <Typography
                  variant="body2"
                  color="success.main"
                  sx={{ fontSize: isMobile ? '0.75rem' : '0.875rem' }}
                >
                  ● Online
                </Typography>
              </Box>
              <Box
                display="flex"
                justifyContent="space-between"
                mb={1}
                sx={{
                  flexDirection: isMobile ? 'column' : 'row',
                  alignItems: isMobile ? 'flex-start' : 'center'
                }}
              >
                <Typography
                  variant="body2"
                  sx={{ fontSize: isMobile ? '0.75rem' : '0.875rem' }}
                >
                  License Server
                </Typography>
                <Typography
                  variant="body2"
                  color="success.main"
                  sx={{ fontSize: isMobile ? '0.75rem' : '0.875rem' }}
                >
                  ● Online
                </Typography>
              </Box>
              <Box
                display="flex"
                justifyContent="space-between"
                mb={1}
                sx={{
                  flexDirection: isMobile ? 'column' : 'row',
                  alignItems: isMobile ? 'flex-start' : 'center'
                }}
              >
                <Typography
                  variant="body2"
                  sx={{ fontSize: isMobile ? '0.75rem' : '0.875rem' }}
                >
                  Authentication
                </Typography>
                <Typography
                  variant="body2"
                  color="success.main"
                  sx={{ fontSize: isMobile ? '0.75rem' : '0.875rem' }}
                >
                  ● Online
                </Typography>
              </Box>
            </Box>
          </CardContent>
        </Card>
      </Box>
    </Box>
  );
};
