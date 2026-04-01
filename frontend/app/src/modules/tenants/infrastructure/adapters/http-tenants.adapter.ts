import * as platformApi from "../api/tenants.api";

export function createHttpTenantsAdapter() {
  return {
    createTenant(payload: object, token: string) {
      return platformApi.createTenant(payload, token);
    },
    upsertTenantFeature(tenantId: string, featureKey: string, payload: object, token: string) {
      return platformApi.upsertTenantFeature(tenantId, featureKey, payload, token);
    }
  };
}
