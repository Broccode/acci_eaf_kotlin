// Basis-Typen für die Control Plane UI

// Export tenant types
export type {
  Tenant,
  TenantStatus,
  CreateTenantRequest,
  UpdateTenantRequest,
  TenantListResponse
} from './tenant';

// Export user types
export type {
  User,
  UserStatus,
  CreateUserRequest,
  UpdateUserRequest,
  UserListResponse,
  PasswordResetRequest
} from './user';

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
