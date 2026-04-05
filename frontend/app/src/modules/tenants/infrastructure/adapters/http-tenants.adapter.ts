import * as platformApi from "../api/tenants.api";

type TenantSummary = {
  id: string;
  key: string;
  displayName: string;
  status: string;
};

type TenantFeature = {
  featureKey: string;
  enabled: boolean;
};

export function createHttpTenantsAdapter() {
  return {
    createTenant(payload: object): Promise<TenantSummary> {
      return platformApi.createTenant(payload) as Promise<TenantSummary>;
    },
    upsertTenantFeature(tenantId: string, featureKey: string, payload: object): Promise<TenantFeature> {
      return platformApi.upsertTenantFeature(tenantId, featureKey, payload) as Promise<TenantFeature>;
    }
  };
}
