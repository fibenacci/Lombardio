import { platformGet, platformPatch, platformPost, platformPut } from "./client";

export function fetchTenants(token) {
  return platformGet("/api/v1/platform/tenants", token);
}

export function createTenant(payload, token) {
  return platformPost("/api/v1/platform/tenants", payload, token);
}

export function updateTenant(id, payload, token) {
  return platformPatch(`/api/v1/platform/tenants/${id}`, payload, token);
}

export function fetchTenantFeatures(tenantId, token) {
  return platformGet(`/api/v1/tenants/${tenantId}/features`, token);
}

export function upsertTenantFeature(tenantId, featureKey, payload, token) {
  return platformPut(`/api/v1/platform/tenants/${tenantId}/features/${featureKey}`, payload, token);
}

export function createTenantUser(tenantId, payload, token) {
  return platformPost(`/api/v1/platform/tenants/${tenantId}/users`, payload, token);
}
