import { apiClient } from "../../../../shared/kernel/http/runtime-api-client";

export function fetchUsers(tenantId, token) {
  return apiClient.get(`/api/v1/tenants/${tenantId}/users`, token);
}

export function createUser(tenantId, payload, token) {
  return apiClient.post(`/api/v1/tenants/${tenantId}/users`, payload, token);
}

export function fetchBranches(tenantId, token) {
  return apiClient.get(`/api/v1/tenants/${tenantId}/branches`, token);
}

export function createBranch(tenantId, payload, token) {
  return apiClient.post(`/api/v1/tenants/${tenantId}/branches`, payload, token);
}

export function updateUser(id, payload, token) {
  return apiClient.patch(`/api/v1/tenants/${payload.tenantId}/users/${id}`, payload, token);
}

export function fetchRoles(tenantId, token) {
  return apiClient.get(`/api/v1/tenants/${tenantId}/roles`, token).then((roles) =>
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
