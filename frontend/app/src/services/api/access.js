import { get, patch, post } from "./client";

export function fetchUsers(tenantId, token) {
  return get(`/api/v1/tenants/${tenantId}/users`, token);
}

export function createUser(tenantId, payload, token) {
  return post(`/api/v1/tenants/${tenantId}/users`, payload, token);
}

export function fetchBranches(tenantId, token) {
  return get(`/api/v1/tenants/${tenantId}/branches`, token);
}

export function createBranch(tenantId, payload, token) {
  return post(`/api/v1/tenants/${tenantId}/branches`, payload, token);
}

export function updateUser(id, payload, token) {
  return patch(`/api/v1/tenants/${payload.tenantId}/users/${id}`, payload, token);
}

export function fetchRoles(tenantId, token) {
  return get(`/api/v1/tenants/${tenantId}/roles`, token).then((roles) =>
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

export function createRole(tenantId, payload, token) {
  void tenantId;
  void payload;
  void token;
  return Promise.reject(new Error("Tenant-specific role management is not implemented by the current platform API."));
}

export function updateRole(id, payload, token) {
  void id;
  void payload;
  void token;
  return Promise.reject(new Error("Tenant-specific role management is not implemented by the current platform API."));
}

export function fetchPermissions(token) {
  void token;
  return Promise.resolve([]);
}
