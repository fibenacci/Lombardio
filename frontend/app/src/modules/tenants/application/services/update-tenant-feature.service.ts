export function createUpdateTenantFeatureService(adapter: { upsertTenantFeature: (tenantId: string, featureKey: string, payload: object) => Promise<any> }) {
  return {
    async execute(tenantId: string, featureKey: string, enabled: boolean) {
      return adapter.upsertTenantFeature(tenantId, featureKey, { enabled });
    }
  };
}
