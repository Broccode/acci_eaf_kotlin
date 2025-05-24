import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Fade,
  Paper,
  TextField,
  Typography,
  useMediaQuery,
  useTheme
} from '@mui/material';
import type React from 'react';
import { useEffect, useState } from 'react';
import { authProvider } from '../services/authProvider';

interface LoginPageProps {
  onLoginSuccess?: () => void;
}

// Login-Seite als eigenständige Komponente ohne React-Admin Abhängigkeiten
export const LoginPage: React.FC<LoginPageProps> = ({ onLoginSuccess }) => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [attemptCount, setAttemptCount] = useState(0);
  const [isBlocked, setIsBlocked] = useState(false);
  const [blockTimeRemaining, setBlockTimeRemaining] = useState(0);

  const theme = useTheme();

  // Bessere Breakpoint-Erkennung für Desktop/Tablet/Mobile
  const isMobile = useMediaQuery(theme.breakpoints.down('sm')); // < 600px
  const isTablet = useMediaQuery(theme.breakpoints.between('sm', 'md')); // 600px - 900px
  const isDesktop = useMediaQuery(theme.breakpoints.up('md')); // >= 900px

  // Debug-Informationen für Breakpoints
  useEffect(() => {
    if (import.meta.env.DEV) {
      console.log('📱 Breakpoint Debug:', {
        isMobile,
        isTablet,
        isDesktop,
        windowWidth: window.innerWidth,
        windowHeight: window.innerHeight
      });
    }
  }, [isMobile, isTablet, isDesktop]);

  // Countdown für Blockierung bei zu vielen fehlgeschlagenen Versuchen
  useEffect(() => {
    if (blockTimeRemaining > 0) {
      const timer = setTimeout(() => {
        setBlockTimeRemaining(prev => prev - 1);
      }, 1000);
      return () => clearTimeout(timer);
    }

    if (isBlocked && blockTimeRemaining === 0) {
      setIsBlocked(false);
      setAttemptCount(0);
    }
  }, [blockTimeRemaining, isBlocked]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (isBlocked) {
      return;
    }

    if (!username.trim() || !password.trim()) {
      setError('Bitte geben Sie Benutzername und Passwort ein.');
      return;
    }

    setError('');
    setLoading(true);

    try {
      // Direkt authProvider verwenden ohne React-Admin Hooks
      console.log('🎯 LoginPage: Starting login process...');
      await authProvider.login({ username, password });
      console.log('✅ LoginPage: Login successful!');
      setAttemptCount(0);

      // Kurz warten damit der Token sicher gespeichert ist
      await new Promise(resolve => setTimeout(resolve, 100));

      // Überprüfe ob Token wirklich gespeichert wurde
      const token = localStorage.getItem('auth_token');
      console.log('🔍 LoginPage: Verification - Token stored:', !!token);

      if (onLoginSuccess) {
        console.log('📞 LoginPage: Calling onLoginSuccess callback');
        onLoginSuccess();
      } else {
        console.log('🔄 LoginPage: No callback provided, forcing page reload');
        // Force page reload um den Auth-State in der App zu aktualisieren
        window.location.reload();
      }
    } catch (err) {
      console.log('❌ LoginPage: Login failed:', err);
      const newAttemptCount = attemptCount + 1;
      setAttemptCount(newAttemptCount);

      // Nach 3 fehlgeschlagenen Versuchen kurze Blockierung
      if (newAttemptCount >= 3) {
        setIsBlocked(true);
        setBlockTimeRemaining(30); // 30 Sekunden Blockierung
        setError('Zu viele fehlgeschlagene Anmeldeversuche. Bitte warten Sie 30 Sekunden.');
      } else {
        setError('Anmeldung fehlgeschlagen. Bitte überprüfen Sie Ihre Zugangsdaten.');
      }
    } finally {
      setLoading(false);
    }
  };

  // Responsive Styling-Variablen
  const titleVariant = isMobile ? 'h6' : isTablet ? 'h5' : 'h3';
  const subtitleVariant = isMobile ? 'body2' : isTablet ? 'body1' : 'h5';
  const fieldSize = isMobile ? 'small' : 'medium';

  // Mobile/Tablet Layout: Zentrierte Karte
  if (isMobile || isTablet) {
    const cardMinWidth = isMobile ? '90vw' : '400px';
    const cardMaxWidth = isMobile ? '90vw' : '500px';
    const cardPadding = isMobile ? 1 : 2;

    return (
      <Box
        display="flex"
        justifyContent="center"
        alignItems="center"
        minHeight="100vh"
        bgcolor="#f5f5f5"
        p={isMobile ? 1 : 2}
      >
        <Fade in={true} timeout={600}>
          <Card
            sx={{
              minWidth: cardMinWidth,
              maxWidth: cardMaxWidth,
              p: cardPadding,
              borderRadius: 2,
              boxShadow: 3
            }}
          >
            <CardContent sx={{ p: cardPadding }}>
              <Box textAlign="center" mb={isMobile ? 2 : 3}>
                <Typography
                  variant={titleVariant}
                  component="h1"
                  gutterBottom
                  color="primary.main"
                  fontWeight="bold"
                >
                  ACCI EAF
                </Typography>
                <Typography
                  variant={subtitleVariant}
                  color="text.secondary"
                >
                  Control Plane
                </Typography>
              </Box>

              {error && (
                <Fade in={true}>
                  <Alert
                    severity="error"
                    sx={{
                      mb: 2,
                      fontSize: isMobile ? '0.875rem' : '1rem'
                    }}
                  >
                    {error}
                    {isBlocked && blockTimeRemaining > 0 && (
                      <Typography variant="body2" sx={{ mt: 1 }}>
                        Verbleibende Zeit: {blockTimeRemaining}s
                      </Typography>
                    )}
                  </Alert>
                </Fade>
              )}

              <form onSubmit={handleSubmit}>
                <TextField
                  label="Benutzername"
                  type="text"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  fullWidth
                  margin="normal"
                  required
                  autoComplete="username"
                  autoFocus
                  disabled={loading || isBlocked}
                  size={fieldSize}
                  variant="outlined"
                />

                <TextField
                  label="Passwort"
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  fullWidth
                  margin="normal"
                  required
                  autoComplete="current-password"
                  disabled={loading || isBlocked}
                  size={fieldSize}
                  variant="outlined"
                />

                <Button
                  type="submit"
                  variant="contained"
                  fullWidth
                  disabled={loading || isBlocked}
                  sx={{
                    mt: 3,
                    mb: 2,
                    py: isMobile ? 1 : 1.5,
                    fontSize: isMobile ? '0.875rem' : '1rem'
                  }}
                >
                  {loading ? 'Anmelden...' : isBlocked ? `Blockiert (${blockTimeRemaining}s)` : 'Anmelden'}
                </Button>
              </form>

              <Box mt={2}>
                <Typography
                  variant="body2"
                  color="text.secondary"
                  textAlign="center"
                  sx={{ fontSize: isMobile ? '0.75rem' : '0.875rem' }}
                >
                  Demo-Zugangsdaten: admin / admin123
                </Typography>
              </Box>

              <Box mt={2}>
                <Typography
                  variant="body2"
                  color="text.secondary"
                  textAlign="center"
                  sx={{ fontSize: isMobile ? '0.7rem' : '0.75rem' }}
                >
                  Passwort vergessen? Kontaktieren Sie Ihren Administrator.
                </Typography>
              </Box>

              {/* Debug-Info nur in Development */}
              {import.meta.env.DEV && (
                <Box mt={2} p={1} bgcolor="grey.100" borderRadius={1}>
                  <Typography variant="caption" display="block">
                    Debug: {isMobile ? 'Mobile' : 'Tablet'} ({window.innerWidth}px)
                  </Typography>
                </Box>
              )}
            </CardContent>
          </Card>
        </Fade>
      </Box>
    );
  }

  // Desktop Layout: Zweispaltig mit Branding
  return (
    <Box
      display="flex"
      minHeight="100vh"
      width="100vw"
      bgcolor="#f5f5f5"
      position="fixed"
      top={0}
      left={0}
    >
      <Fade in={true} timeout={600}>
        <Box display="flex" width="100%" minHeight="100vh">
          {/* Linke Seite: Branding & Information */}
          <Box
            flex="1 1 70%"
            display="flex"
            flexDirection="column"
            justifyContent="center"
            alignItems="center"
            sx={{
              background: 'linear-gradient(135deg, #1976d2 0%, #1565c0 100%)',
              color: 'white',
              px: 4,
              minHeight: '100vh'
            }}
          >
            <Typography
              variant="h2"
              component="h1"
              gutterBottom
              fontWeight="bold"
              textAlign="center"
            >
              ACCI EAF
            </Typography>
            <Typography
              variant="h4"
              component="h2"
              gutterBottom
              textAlign="center"
              sx={{ mb: 4 }}
            >
              Enterprise Application Framework
            </Typography>
            <Typography
              variant="h6"
              textAlign="center"
              sx={{ maxWidth: '600px', lineHeight: 1.6, opacity: 0.9 }}
            >
              Moderne, skalierbare Lösung für Enterprise-Anwendungen mit
              Multi-Tenancy, RBAC und umfassendem Lizenzmanagement.
            </Typography>
            <Box mt={4}>
              <Typography variant="h5" gutterBottom textAlign="center">
                Control Plane
              </Typography>
              <Typography variant="body1" textAlign="center" sx={{ opacity: 0.8 }}>
                Administrative Benutzeroberfläche
              </Typography>
            </Box>
          </Box>

          {/* Rechte Seite: Login-Formular */}
          <Box
            flex="1 1 30%"
            display="flex"
            flexDirection="column"
            justifyContent="center"
            alignItems="center"
            px={4}
            bgcolor="white"
            sx={{ minHeight: '100vh', minWidth: '400px' }}
          >
            <Paper
              elevation={0}
              sx={{
                width: '100%',
                maxWidth: '400px',
                p: 4
              }}
            >
              <Box textAlign="center" mb={4}>
                <Typography
                  variant="h4"
                  component="h1"
                  gutterBottom
                  color="primary.main"
                  fontWeight="bold"
                >
                  Anmelden
                </Typography>
                <Typography
                  variant="body1"
                  color="text.secondary"
                >
                  Melden Sie sich an, um fortzufahren
                </Typography>
              </Box>

              {error && (
                <Fade in={true}>
                  <Alert
                    severity="error"
                    sx={{ mb: 3 }}
                  >
                    {error}
                    {isBlocked && blockTimeRemaining > 0 && (
                      <Typography variant="body2" sx={{ mt: 1 }}>
                        Verbleibende Zeit: {blockTimeRemaining}s
                      </Typography>
                    )}
                  </Alert>
                </Fade>
              )}

              <form onSubmit={handleSubmit}>
                <TextField
                  label="Benutzername"
                  type="text"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  fullWidth
                  margin="normal"
                  required
                  autoComplete="username"
                  autoFocus
                  disabled={loading || isBlocked}
                  size="medium"
                  variant="outlined"
                  sx={{ mb: 2 }}
                />

                <TextField
                  label="Passwort"
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  fullWidth
                  margin="normal"
                  required
                  autoComplete="current-password"
                  disabled={loading || isBlocked}
                  size="medium"
                  variant="outlined"
                  sx={{ mb: 3 }}
                />

                <Button
                  type="submit"
                  variant="contained"
                  fullWidth
                  disabled={loading || isBlocked}
                  size="large"
                  sx={{
                    py: 1.5,
                    fontSize: '1.1rem',
                    mb: 3
                  }}
                >
                  {loading ? 'Anmelden...' : isBlocked ? `Blockiert (${blockTimeRemaining}s)` : 'Anmelden'}
                </Button>
              </form>

              <Box>
                <Typography
                  variant="body2"
                  color="text.secondary"
                  textAlign="center"
                  sx={{ mb: 2 }}
                >
                  Demo-Zugangsdaten: admin / admin123
                </Typography>
                <Typography
                  variant="body2"
                  color="text.secondary"
                  textAlign="center"
                >
                  Passwort vergessen? Kontaktieren Sie Ihren Administrator.
                </Typography>
              </Box>

              {/* Debug-Info nur in Development */}
              {import.meta.env.DEV && (
                <Box mt={3} p={2} bgcolor="grey.100" borderRadius={1}>
                  <Typography variant="caption" display="block">
                    Debug: Desktop ({window.innerWidth}px)
                  </Typography>
                </Box>
              )}
            </Paper>
          </Box>
        </Box>
      </Fade>
    </Box>
  );
};
