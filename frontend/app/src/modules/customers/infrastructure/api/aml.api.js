import { BASE_URLS, createApiClient } from "../../../../shared/kernel/http/runtime-api-client";

const apiClient = createApiClient(BASE_URLS.platform);

export function fetchAmlStatus(tenantId, customerId) {
  return apiClient.get(`/api/v1/platform/operator/tenants/${tenantId}/customers/${customerId}/aml`);
}

export function updateAmlStatus(tenantId, customerId, payload) {
  return apiClient.post(`/api/v1/platform/operator/tenants/${tenantId}/customers/${customerId}/aml`, payload);
}

export function assessAmlOrigination(tenantId, customerId, payload) {
  return apiClient.post(
    `/api/v1/platform/operator/tenants/${tenantId}/customers/${customerId}/aml/origination-check`,
    payload
  );
}
