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
  return patch(`/api/v1/users/${id}`, payload, token);
}

export function fetchRoles(tenantId, token) {
  return get(`/api/v1/tenants/${tenantId}/roles`, token);
}

export function createRole(tenantId, payload, token) {
  return post(`/api/v1/tenants/${tenantId}/roles`, payload, token);
}

export function updateRole(id, payload, token) {
  return patch(`/api/v1/roles/${id}`, payload, token);
}

export function fetchPermissions(token) {
  return get("/api/v1/permissions", token);
}
