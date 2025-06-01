# Feature-Based Architecture

This application follows a feature-based architecture where code is organized by feature domain rather than by technical layers.

## Structure

Each feature has its own directory containing all related code:

```
features/
├── users/           # User management feature
│   ├── components/  # React components for users
│   ├── services/    # API services and data providers
│   └── index.ts     # Feature exports
├── tenants/         # Tenant management feature
│   ├── components/  # React components for tenants
│   ├── services/    # API services and data providers
│   └── index.ts     # Feature exports
├── roles/           # Role management feature
│   ├── services/    # Role service API
│   └── index.ts     # Feature exports
└── auth/            # Authentication feature
    ├── services/    # Auth provider
    └── index.ts     # Feature exports
```

## Benefits

- **Scalability**: Features can be developed independently
- **Maintainability**: Related code stays together
- **Discoverability**: Easy to find all code for a feature
- **Modularity**: Features can be moved or extracted easily
- **Team Collaboration**: Teams can own specific features

## Guidelines

1. **Keep features independent**: Minimize cross-feature dependencies
2. **Use barrel exports**: Export through index.ts for clean imports
3. **Shared code goes to core**: Common utilities belong in src/core/
4. **Types stay global**: Keep types in src/types/ for cross-feature use

## Example Import

```typescript
// Instead of deep imports:
import { UserList } from '../../../components/users/UserList';

// Use feature imports:
import { UserList } from '@/features/users';
```
