import { BASE_URLS, createApiClient } from "./client";

const apiClient = createApiClient(BASE_URLS.origination);

export function fetchValuationGuidelines(tenantId, token) {
  return apiClient.get(`/api/v1/tenants/${tenantId}/valuation-guidelines`, token);
}

export function createLoan(tenantId, payload, token) {
  return apiClient.post(`/api/v1/tenants/${tenantId}/loans`, payload, token);
}

export function fetchLoans(tenantId, token, customerId = null) {
  const suffix = customerId ? `?customerId=${encodeURIComponent(customerId)}` : "";
  return apiClient.get(`/api/v1/tenants/${tenantId}/loans${suffix}`, token);
}
