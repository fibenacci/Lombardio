import * as platformApi from "../api/tenants.api";

export function createHttpTenantsAdapter() {
  return {
    createTenant(payload: object) {
      return platformApi.createTenant(payload);
    },
    upsertTenantFeature(tenantId: string, featureKey: string, payload: object) {
      return platformApi.upsertTenantFeature(tenantId, featureKey, payload);
    }
  };
}
