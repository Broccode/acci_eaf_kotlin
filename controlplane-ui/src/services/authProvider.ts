import type { AuthProvider } from 'react-admin';
import type { AuthResponse, LoginRequest } from '../types';

// Mock API Service für Authentifizierung
class AuthService {
  constructor() {
    // Debug: localStorage Status beim Start anzeigen
    if (import.meta.env.DEV) {
      const token = localStorage.getItem('auth_token');
      const userData = localStorage.getItem('user_data');
      console.log('🔧 Development Mode - localStorage Status:');
      console.log('  Token:', token ? 'exists' : 'missing');
      console.log('  UserData:', userData ? 'exists' : 'missing');

      // Nur beim allerersten Load leeren (wenn spezielle Flag gesetzt ist)
      const shouldClear = sessionStorage.getItem('clear_storage_on_start');
      if (shouldClear === 'true') {
        console.log('🧹 Clearing localStorage as requested');
        localStorage.removeItem('auth_token');
        localStorage.removeItem('user_data');
        sessionStorage.removeItem('clear_storage_on_start');
      }
    }
  }

  async login(credentials: LoginRequest): Promise<AuthResponse> {
    console.log('🔐 Login attempt started:', credentials.username);

    // Simuliere kurze Netzwerk-Verzögerung
    await new Promise(resolve => setTimeout(resolve, 500));

    // Für MVP: Mock-Implementierung
    // In der finalen Version wird hier die echte API aufgerufen
    if (credentials.username === 'admin' && credentials.password === 'admin123') {
      const mockResponse: AuthResponse = {
        accessToken: `mock-jwt-token-${Date.now()}`,
        expiresIn: 3600,
        user: {
          userId: '1',
          username: credentials.username,
          tenantId: '1',
          roles: ['TENANT_ADMIN'],
        },
      };

      // Token in localStorage speichern
      console.log('✅ Login successful, storing token:', mockResponse.accessToken);
      localStorage.setItem('auth_token', mockResponse.accessToken);
      localStorage.setItem('user_data', JSON.stringify(mockResponse.user));

      // Verify storage worked
      const storedToken = localStorage.getItem('auth_token');
      const storedUser = localStorage.getItem('user_data');
      console.log('📦 Storage verification:');
      console.log('  Stored token:', storedToken);
      console.log('  Stored user:', storedUser);

      return mockResponse;
    }

    console.log('❌ Login failed - invalid credentials');
    throw new Error('Invalid credentials');
  }

  logout(): void {
    console.log('🚪 Logout - clearing localStorage');
    localStorage.removeItem('auth_token');
    localStorage.removeItem('user_data');
  }

  getCurrentUser() {
    const userData = localStorage.getItem('user_data');
    const user = userData ? JSON.parse(userData) : null;
    console.log('👤 Get current user:', user);
    return user;
  }

  isAuthenticated(): boolean {
    const token = localStorage.getItem('auth_token');
    const isAuth = !!token;
    console.log('🔍 Check authentication:', isAuth, 'Token:', token ? `${token.substring(0, 20)}...` : 'missing');
    return isAuth;
  }
}

const authService = new AuthService();

export const authProvider: AuthProvider = {
  login: async ({ username, password }) => {
    try {
      console.log('🎯 AuthProvider.login called with:', username);
      await authService.login({ username, password });
      console.log('🎯 Login provider: Success, resolving');
      return Promise.resolve();
    } catch (error) {
      console.log('🚨 Login provider: Failed, rejecting', error);
      return Promise.reject(new Error('Login failed'));
    }
  },

  logout: () => {
    console.log('🚪 Logout provider: Clearing auth');
    authService.logout();
    return Promise.resolve();
  },

  checkAuth: () => {
    const isAuth = authService.isAuthenticated();
    console.log('🛡️ CheckAuth provider result:', isAuth ? 'authenticated' : 'not authenticated');

    if (isAuth) {
      console.log('✅ CheckAuth: User is authenticated, resolving');
      return Promise.resolve();
    }
    console.log('❌ CheckAuth: User not authenticated, rejecting to force login');
    // React-Admin erwartet einen Fehler um zur Login-Seite zu redirecten
    return Promise.reject(new Error('Not authenticated - redirecting to login'));
  },

  checkError: (error) => {
    const status = error.status;
    console.log('🔥 CheckError called with status:', status);

    if (status === 401 || status === 403) {
      console.log('🚨 Authentication error, logging out');
      authService.logout();
      return Promise.reject(new Error('Authentication error'));
    }
    return Promise.resolve();
  },

  getIdentity: () => {
    const user = authService.getCurrentUser();
    console.log('🆔 GetIdentity called, user:', user);

    if (user) {
      return Promise.resolve({
        id: user.userId,
        fullName: user.username,
        username: user.username,
        ...user
      });
    }

    console.log('❌ GetIdentity: No user found, rejecting');
    return Promise.reject(new Error('No user identity available'));
  },

  getPermissions: () => {
    const user = authService.getCurrentUser();
    console.log('🔑 GetPermissions called, user:', user);

    if (user?.roles) {
      return Promise.resolve(user.roles);
    }

    console.log('❌ GetPermissions: No permissions found, rejecting');
    return Promise.reject(new Error('No permissions available'));
  },
};

// Für Development: Funktion um localStorage beim nächsten Reload zu leeren
export const clearStorageOnNextReload = () => {
  if (import.meta.env.DEV) {
    sessionStorage.setItem('clear_storage_on_start', 'true');
    console.log('🧹 Scheduled localStorage clear for next reload');
  }
};
