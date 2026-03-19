import { amlGet, amlPost } from "./client";

export function fetchAmlStatus(tenantId, customerId, token) {
  return amlGet(`/api/v1/tenants/${tenantId}/customers/${customerId}/aml`, token);
}

export function updateAmlStatus(tenantId, customerId, payload, token) {
  return amlPost(`/api/v1/tenants/${tenantId}/customers/${customerId}/aml`, payload, token);
}

export function assessAmlOrigination(tenantId, customerId, payload, token) {
  return amlPost(`/api/v1/tenants/${tenantId}/customers/${customerId}/aml/origination-check`, payload, token);
}
