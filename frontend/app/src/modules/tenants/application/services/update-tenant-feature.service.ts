type TenantFeaturePayload = {
  enabled: boolean;
};

type TenantFeature = {
  featureKey: string;
  enabled: boolean;
};

export function createUpdateTenantFeatureService(
  adapter: { upsertTenantFeature: (tenantId: string, featureKey: string, payload: TenantFeaturePayload) => Promise<TenantFeature> }
) {
  return {
    async execute(tenantId: string, featureKey: string, enabled: boolean) {
      return adapter.upsertTenantFeature(tenantId, featureKey, { enabled });
    }
  };
}
