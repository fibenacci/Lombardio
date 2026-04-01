import { kycGet, kycPost } from "./client";

export function fetchKycStatus(tenantId, customerId, token) {
  return kycGet(`/api/v1/tenants/${tenantId}/customers/${customerId}/kyc`, token);
}

export function fetchKycDocuments(tenantId, customerId, token) {
  return kycGet(`/api/v1/tenants/${tenantId}/customers/${customerId}/kyc/documents`, token);
}

export function updateKycStatus(tenantId, customerId, payload, token) {
  return kycPost(`/api/v1/tenants/${tenantId}/customers/${customerId}/kyc`, payload, token);
}

export function prefillKycDocument(tenantId, customerId, payload, token) {
  return kycPost(`/api/v1/tenants/${tenantId}/customers/${customerId}/kyc/document-prefill`, payload, token);
}
