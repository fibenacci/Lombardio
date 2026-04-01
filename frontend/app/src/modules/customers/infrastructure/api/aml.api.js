import { BASE_URLS, createApiClient } from "../../../../shared/kernel/http/runtime-api-client";

const apiClient = createApiClient(BASE_URLS.aml);

export function fetchAmlStatus(tenantId, customerId, token) {
  return apiClient.get(`/api/v1/tenants/${tenantId}/customers/${customerId}/aml`, token);
}

export function updateAmlStatus(tenantId, customerId, payload, token) {
  return apiClient.post(`/api/v1/tenants/${tenantId}/customers/${customerId}/aml`, payload, token);
}

export function assessAmlOrigination(tenantId, customerId, payload, token) {
  return apiClient.post(
    `/api/v1/tenants/${tenantId}/customers/${customerId}/aml/origination-check`,
    payload,
    token
  );
}
