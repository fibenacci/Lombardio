import { BASE_URLS, createApiClient } from "../../../../shared/kernel/http/runtime-api-client";

const apiClient = createApiClient(BASE_URLS.kyc);

export function fetchKycStatus(tenantId, customerId, token) {
  return apiClient.get(`/api/v1/tenants/${tenantId}/customers/${customerId}/kyc`, token);
}

export function fetchKycDocuments(tenantId, customerId, token) {
  return apiClient.get(`/api/v1/tenants/${tenantId}/customers/${customerId}/kyc/documents`, token);
}

export function updateKycStatus(tenantId, customerId, payload, token) {
  return apiClient.post(`/api/v1/tenants/${tenantId}/customers/${customerId}/kyc`, payload, token);
}

export function prefillKycDocument(tenantId, customerId, payload, token) {
  return apiClient.post(
    `/api/v1/tenants/${tenantId}/customers/${customerId}/kyc/document-prefill`,
    payload,
    token
  );
}
