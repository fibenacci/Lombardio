import { BASE_URLS, createApiClient } from "../../../../shared/kernel/http/runtime-api-client";

const apiClient = createApiClient(BASE_URLS.platform);

export function fetchTenants(token) {
  return apiClient.get("/api/v1/platform/tenants", token);
}

export function createTenant(payload, token) {
  return apiClient.post("/api/v1/platform/tenants", payload, token);
}

export function updateTenant(id, payload, token) {
  return apiClient.patch(`/api/v1/platform/tenants/${id}`, payload, token);
}

export function fetchTenantFeatures(tenantId, token) {
  return apiClient.get(`/api/v1/tenants/${tenantId}/features`, token);
}

export function upsertTenantFeature(tenantId, featureKey, payload, token) {
  return apiClient.put(`/api/v1/platform/tenants/${tenantId}/features/${featureKey}`, payload, token);
}
