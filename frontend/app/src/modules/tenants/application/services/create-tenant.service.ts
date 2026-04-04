export function createCreateTenantService(adapter: { createTenant: (payload: object) => Promise<any> }) {
  return {
    async execute(payload: { key: string; displayName: string; status: string }) {
      return adapter.createTenant(payload);
    }
  };
}
