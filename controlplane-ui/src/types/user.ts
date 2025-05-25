import type { RaRecord } from 'react-admin';

/**
 * User status enum values
 */
export type UserStatus = 'ACTIVE' | 'LOCKED' | 'DISABLED';

/**
 * User entity interface - extends RaRecord for React-Admin compatibility
 */
export interface User extends RaRecord {
  id: string;
  username: string;
  email: string;
  status: UserStatus;
  tenantId: string;
  createdAt: string;
  updatedAt: string;
}

/**
 * Create user request interface
 */
export interface CreateUserRequest {
  username: string;
  email: string;
  password: string;
  tenantId: string;
  status?: UserStatus;
}

/**
 * Update user request interface - extends RaRecord for React-Admin compatibility
 */
export interface UpdateUserRequest extends RaRecord {
  id: string;
  email?: string;
  status?: UserStatus;
  tenantId: string;
}

/**
 * User list response interface for React-Admin
 */
export interface UserListResponse {
  data: User[];
  total: number;
}

/**
 * Password reset request interface
 */
export interface PasswordResetRequest {
  userId: string;
  newPassword: string;
  tenantId: string;
}
