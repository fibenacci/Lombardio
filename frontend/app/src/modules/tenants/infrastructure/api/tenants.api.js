import { BASE_URLS, createApiClient } from "../../../../shared/kernel/http/runtime-api-client";

const apiClient = createApiClient(BASE_URLS.platform);

export function fetchTenants() {
  return apiClient.get("/api/v1/platform/operator/tenants");
}

export function createTenant(payload) {
  return apiClient.post("/api/v1/platform/operator/tenants", payload);
}

export function updateTenant(id, payload) {
  return apiClient.patch(`/api/v1/platform/operator/tenants/${id}`, payload);
}

export function fetchTenantFeatures(tenantId) {
  return apiClient.get(`/api/v1/platform/operator/tenants/${tenantId}/features`);
}

export function upsertTenantFeature(tenantId, featureKey, payload) {
  return apiClient.put(`/api/v1/platform/operator/tenants/${tenantId}/features/${featureKey}`, payload);
}
