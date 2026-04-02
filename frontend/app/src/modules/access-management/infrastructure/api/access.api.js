import { BASE_URLS, createApiClient } from "../../../../shared/kernel/http/runtime-api-client";

const apiClient = createApiClient(BASE_URLS.platform);

export function fetchUsers(tenantId) {
  return apiClient.get(`/api/v1/tenants/${tenantId}/users`);
}

export function createUser(tenantId, payload) {
  return apiClient.post(`/api/v1/tenants/${tenantId}/users`, payload);
}

export function fetchBranches(tenantId) {
  return apiClient.get(`/api/v1/tenants/${tenantId}/branches`);
}

export function createBranch(tenantId, payload) {
  return apiClient.post(`/api/v1/tenants/${tenantId}/branches`, payload);
}

export function updateUser(id, payload) {
  return apiClient.patch(`/api/v1/tenants/${payload.tenantId}/users/${id}`, payload);
}

export function fetchRoles(tenantId) {
  return apiClient.get(`/api/v1/tenants/${tenantId}/roles`).then((roles) =>
    roles.map((role) => ({
      id: role,
      key: role,
      displayName: role,
      description: "",
      active: true,
      permissionKeys: []
    }))
  );
}
