import '@testing-library/jest-dom';
import type { RaRecord } from 'react-admin';

// Mock environment variables
Object.defineProperty(process, 'env', {
  value: {
    VITE_API_BASE_URL: 'http://localhost:8080',
  },
});

// Mock import.meta.env for Vite
Object.defineProperty(globalThis, 'import', {
  value: {
    meta: {
      env: {
        VITE_API_BASE_URL: 'http://localhost:8080',
      },
    },
  },
});

// Mock React-Admin hooks
jest.mock('react-admin', () => ({
  ...jest.requireActual('react-admin'),
  useNotify: () => jest.fn(),
  useRefresh: () => jest.fn(),
  useUpdate: () => [jest.fn()],
  useDelete: () => [jest.fn()],
  useRecordContext: () => ({
    id: '1',
    name: 'Test Tenant',
    description: 'Test Description',
    status: 'ACTIVE',
    createdAt: '2024-01-15T10:30:00Z',
    updatedAt: '2024-01-15T10:30:00Z',
  } as RaRecord),
}));
