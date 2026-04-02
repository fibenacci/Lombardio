import { BASE_URLS, createApiClient } from "../../../../shared/kernel/http/runtime-api-client";

const apiClient = createApiClient(BASE_URLS.platform);

export function fetchValuationGuidelines(tenantId) {
  return apiClient.get(`/api/v1/platform/operator/tenants/${tenantId}/valuation-guidelines`);
}

export function createLoan(tenantId, payload) {
  return apiClient.post(`/api/v1/platform/operator/tenants/${tenantId}/loans`, payload);
}

export function fetchLoans(tenantId, customerId = null) {
  const suffix = customerId ? `?customerId=${encodeURIComponent(customerId)}` : "";
  return apiClient.get(`/api/v1/platform/operator/tenants/${tenantId}/loans${suffix}`);
}
