import type { DataProvider } from 'react-admin';
import { tenantDataProvider } from './tenantDataProvider';

// Combined Data Provider für React-Admin
// Verwendet spezifische Provider für verschiedene Ressourcen
export const dataProvider: DataProvider = {
  getList: (resource, params) => {
    if (resource === 'tenants' && tenantDataProvider.getList) {
      return tenantDataProvider.getList(resource, params);
    }

    // Für diese MVP-Version unterstützen wir nur Tenants
    console.log('getList called for unsupported resource:', resource);
    throw new Error(`Resource '${resource}' is not supported yet`);
  },

  getOne: (resource, params) => {
    if (resource === 'tenants' && tenantDataProvider.getOne) {
      return tenantDataProvider.getOne(resource, params);
    }

    console.log('getOne called for unsupported resource:', resource);
    throw new Error(`Resource '${resource}' is not supported yet`);
  },

  getMany: (resource, params) => {
    if (resource === 'tenants') {
      return tenantDataProvider.getMany?.(resource, params) ?? Promise.resolve({ data: [] });
    }

    console.log('getMany called for unsupported resource:', resource);
    throw new Error(`Resource '${resource}' is not supported yet`);
  },

  getManyReference: (resource, params) => {
    if (resource === 'tenants') {
      return tenantDataProvider.getManyReference?.(resource, params) ?? Promise.resolve({ data: [], total: 0 });
    }

    console.log('getManyReference called for unsupported resource:', resource);
    throw new Error(`Resource '${resource}' is not supported yet`);
  },

  create: (resource, params) => {
    if (resource === 'tenants' && tenantDataProvider.create) {
      return tenantDataProvider.create(resource, params);
    }

    console.log('create called for unsupported resource:', resource);
    throw new Error(`Resource '${resource}' is not supported yet`);
  },

  update: (resource, params) => {
    if (resource === 'tenants' && tenantDataProvider.update) {
      return tenantDataProvider.update(resource, params);
    }

    console.log('update called for unsupported resource:', resource);
    throw new Error(`Resource '${resource}' is not supported yet`);
  },

  updateMany: (resource, params) => {
    if (resource === 'tenants') {
      return tenantDataProvider.updateMany?.(resource, params) ?? Promise.resolve({ data: params.ids });
    }

    console.log('updateMany called for unsupported resource:', resource);
    throw new Error(`Resource '${resource}' is not supported yet`);
  },

  delete: (resource, params) => {
    if (resource === 'tenants' && tenantDataProvider.delete) {
      return tenantDataProvider.delete(resource, params);
    }

    console.log('delete called for unsupported resource:', resource);
    throw new Error(`Resource '${resource}' is not supported yet`);
  },

  deleteMany: (resource, params) => {
    if (resource === 'tenants') {
      return tenantDataProvider.deleteMany?.(resource, params) ?? Promise.resolve({ data: params.ids });
    }

    console.log('deleteMany called for unsupported resource:', resource);
    throw new Error(`Resource '${resource}' is not supported yet`);
  },
};
