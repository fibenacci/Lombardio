import { originationGet, originationPost } from "./client";

export function fetchValuationGuidelines(tenantId, token) {
  return originationGet(`/api/v1/tenants/${tenantId}/valuation-guidelines`, token);
}

export function createLoan(tenantId, payload, token) {
  return originationPost(`/api/v1/tenants/${tenantId}/loans`, payload, token);
}

export function fetchLoans(tenantId, token, customerId = null) {
  const suffix = customerId ? `?customerId=${encodeURIComponent(customerId)}` : "";
  return originationGet(`/api/v1/tenants/${tenantId}/loans${suffix}`, token);
}
