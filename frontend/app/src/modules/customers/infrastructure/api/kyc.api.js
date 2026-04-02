import { BASE_URLS, createApiClient } from "../../../../shared/kernel/http/runtime-api-client";

const apiClient = createApiClient(BASE_URLS.platform);

export function fetchKycStatus(tenantId, customerId) {
  return apiClient.get(`/api/v1/platform/operator/tenants/${tenantId}/customers/${customerId}/kyc`);
}

export function fetchKycDocuments(tenantId, customerId) {
  return apiClient.get(`/api/v1/platform/operator/tenants/${tenantId}/customers/${customerId}/kyc/documents`);
}

export function updateKycStatus(tenantId, customerId, payload) {
  return apiClient.post(`/api/v1/platform/operator/tenants/${tenantId}/customers/${customerId}/kyc`, payload);
}

export function prefillKycDocument(tenantId, customerId, payload) {
  return apiClient.post(
    `/api/v1/platform/operator/tenants/${tenantId}/customers/${customerId}/kyc/document-prefill`,
    payload
  );
}
