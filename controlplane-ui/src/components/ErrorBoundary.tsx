import { Alert, Box, Button, Typography } from '@mui/material';
import { Component } from 'react';
import type { ErrorInfo, ReactNode } from 'react';

interface Props {
  children: ReactNode;
}

interface State {
  hasError: boolean;
  error: Error | null;
  errorInfo: ErrorInfo | null;
}

export class ErrorBoundary extends Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      hasError: false,
      error: null,
      errorInfo: null,
    };
  }

  static getDerivedStateFromError(error: Error): State {
    // Update state to show error UI
    return {
      hasError: true,
      error,
      errorInfo: null,
    };
  }

  componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    // Log error to console and potentially to backend
    console.error('Global error caught by ErrorBoundary:', error, errorInfo);

    this.setState({
      hasError: true,
      error,
      errorInfo,
    });

    // In production, this would be sent to error logging service like Sentry
    // For now, just log to console
    if (import.meta.env.PROD) {
      // TODO: Send to error logging service
    }
  }

  handleReload = () => {
    window.location.reload();
  };

  render() {
    if (this.state.hasError) {
      return (
        <Box
          display="flex"
          flexDirection="column"
          alignItems="center"
          justifyContent="center"
          minHeight="100vh"
          p={3}
        >
          <Alert severity="error" sx={{ mb: 3, maxWidth: 600 }}>
            <Typography variant="h6" gutterBottom>
              Ein unerwarteter Fehler ist aufgetreten
            </Typography>
            <Typography variant="body2" paragraph>
              Die Anwendung ist auf einen unerwarteten Fehler gestoßen.
              Bitte laden Sie die Seite neu oder kontaktieren Sie den Administrator,
              falls das Problem weiterhin besteht.
            </Typography>

            {import.meta.env.DEV && this.state.error && (
              <Box sx={{ mt: 2 }}>
                <Typography variant="body2" component="pre" sx={{
                  fontSize: '0.75rem',
                  backgroundColor: '#f5f5f5',
                  p: 1,
                  borderRadius: 1,
                  overflow: 'auto',
                  maxHeight: 200
                }}>
                  {this.state.error.toString()}
                  {this.state.errorInfo?.componentStack}
                </Typography>
              </Box>
            )}
          </Alert>

          <Button
            variant="contained"
            onClick={this.handleReload}
            size="large"
          >
            Seite neu laden
          </Button>
        </Box>
      );
    }

    return this.props.children;
  }
}
