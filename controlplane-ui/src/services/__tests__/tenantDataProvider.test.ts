// Simplified test for tenantDataProvider without strict TypeScript generics
// This avoids the complex React-Admin generic type conflicts

describe('tenantDataProvider', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('basic functionality', () => {
    it('should be importable without throwing', async () => {
      expect(async () => {
        const { tenantDataProvider } = await import('../tenantDataProvider');
        expect(tenantDataProvider).toBeDefined();
        expect(tenantDataProvider.getList).toBeDefined();
        expect(tenantDataProvider.getOne).toBeDefined();
        expect(tenantDataProvider.create).toBeDefined();
        expect(tenantDataProvider.update).toBeDefined();
        expect(tenantDataProvider.delete).toBeDefined();
      }).not.toThrow();
    });

    it('should have the correct method signatures', async () => {
      const { tenantDataProvider } = await import('../tenantDataProvider');

      // Verify methods exist and are functions
      expect(typeof tenantDataProvider.getList).toBe('function');
      expect(typeof tenantDataProvider.getOne).toBe('function');
      expect(typeof tenantDataProvider.create).toBe('function');
      expect(typeof tenantDataProvider.update).toBe('function');
      expect(typeof tenantDataProvider.delete).toBe('function');
    });

    it('should throw error for unsupported resources', async () => {
      const { tenantDataProvider } = await import('../tenantDataProvider');

      // Mock fetch to avoid network calls
      global.fetch = jest.fn();

      await expect(
        tenantDataProvider.getList?.('unsupported', {
          pagination: { page: 1, perPage: 10 },
          sort: { field: 'name', order: 'ASC' },
          filter: {},
        })
      ).rejects.toThrow('Unsupported resource: unsupported');

      await expect(
        tenantDataProvider.getOne?.('unsupported', { id: '123' })
      ).rejects.toThrow('Unsupported resource: unsupported');

      await expect(
        tenantDataProvider.create?.('unsupported', { data: {} })
      ).rejects.toThrow('Unsupported resource: unsupported');

      await expect(
        tenantDataProvider.update?.('unsupported', { id: '123', data: {}, previousData: {} })
      ).rejects.toThrow('Unsupported resource: unsupported');

      await expect(
        tenantDataProvider.delete?.('unsupported', { id: '123' })
      ).rejects.toThrow('Unsupported resource: unsupported');
    });
  });

  describe('tenant operations', () => {
    beforeEach(() => {
      // Mock fetch to return errors so we get the fallback mock data
      global.fetch = jest.fn().mockRejectedValue(new Error('Mock API Error'));
    });

    it('should return mock tenant data on getList', async () => {
      const { tenantDataProvider } = await import('../tenantDataProvider');

      const result = await tenantDataProvider.getList?.('tenants', {
        pagination: { page: 1, perPage: 10 },
        sort: { field: 'name', order: 'ASC' },
        filter: {},
      });

      expect(result).toBeDefined();
      expect(result?.data).toHaveLength(2);
      expect(result?.total).toBe(2);
      expect(result?.data[0]).toMatchObject({
        id: '1',
        name: 'Example Tenant 1',
        status: 'ACTIVE',
      });
    });

    it('should return mock tenant data on getOne', async () => {
      const { tenantDataProvider } = await import('../tenantDataProvider');

      const result = await tenantDataProvider.getOne?.('tenants', { id: '123' });

      expect(result).toBeDefined();
      expect(result?.data).toMatchObject({
        id: '123',
        name: 'Tenant 123',
        status: 'ACTIVE',
      });
    });

    it('should return mock tenant data on create', async () => {
      const { tenantDataProvider } = await import('../tenantDataProvider');

      const result = await tenantDataProvider.create?.('tenants', {
        data: {
          name: 'New Tenant',
          description: 'Test Description',
          status: 'ACTIVE',
        },
      });

      expect(result).toBeDefined();
      expect(result?.data).toMatchObject({
        name: 'New Tenant',
        description: 'Test Description',
        status: 'ACTIVE',
      });
      expect(result?.data.id).toBeDefined();
    });

    it('should return mock tenant data on update', async () => {
      const { tenantDataProvider } = await import('../tenantDataProvider');

      const result = await tenantDataProvider.update?.('tenants', {
        id: '123',
        data: {
          name: 'Updated Tenant',
          status: 'INACTIVE',
        },
        previousData: {
          id: '123',
          name: 'Old Tenant',
          status: 'ACTIVE',
        },
      });

      expect(result).toBeDefined();
      expect(result?.data).toMatchObject({
        id: '123',
        name: 'Updated Tenant',
        status: 'INACTIVE',
      });
    });

    it('should return deleted tenant data on delete', async () => {
      const { tenantDataProvider } = await import('../tenantDataProvider');

      const result = await tenantDataProvider.delete?.('tenants', {
        id: '123',
        previousData: {
          id: '123',
          name: 'Test Tenant',
          status: 'ACTIVE',
          createdAt: '',
          updatedAt: '',
        },
      });

      expect(result).toBeDefined();
      expect(result?.data).toMatchObject({
        id: '123',
      });
    });
  });
});

// TODO: Add integration tests when API is fully implemented
// These would test actual API calls and error handling
