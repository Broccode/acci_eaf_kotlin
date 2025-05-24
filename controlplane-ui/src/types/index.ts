// Basis-Typen für die Control Plane UI

export interface User {
  id: string;
  username: string;
  email: string;
  status: 'ACTIVE' | 'INACTIVE' | 'LOCKED_BY_ADMIN' | 'DISABLED_BY_ADMIN';
  tenantId: string;
  createdAt: string;
  updatedAt: string;
}

export interface Tenant {
  tenantId: string;
  name: string;
  status: 'ACTIVE' | 'INACTIVE' | 'SUSPENDED' | 'PENDING_VERIFICATION';
  createdAt: string;
  updatedAt: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken?: string;
  expiresIn: number;
  user: {
    userId: string;
    username: string;
    tenantId: string;
    roles: string[];
  };
}

export interface LoginRequest {
  username: string;
  password: string;
  tenantId?: string;
}

// Layout Props für bessere TypeScript-Unterstützung
export interface LayoutProps {
  children: React.ReactNode;
}
