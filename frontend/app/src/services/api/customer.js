import { BASE_URLS, createApiClient } from "./client";

const apiClient = createApiClient(BASE_URLS.customer);

function normalizeCustomerCollection(payload) {
  if (Array.isArray(payload)) {
    return payload;
  }

  if (Array.isArray(payload?.items)) {
    return payload.items;
  }

  if (Array.isArray(payload?.content)) {
    return payload.content;
  }

  if (payload && typeof payload === "object" && "id" in payload) {
    return [payload];
  }

  return [];
}

export async function searchCustomers(tenantId, query, token) {
  const payload = await apiClient.get(
    `/api/v1/tenants/${tenantId}/customers?query=${encodeURIComponent(query ?? "")}`,
    token
  );
  return normalizeCustomerCollection(payload);
}

export function createCustomer(tenantId, payload, token) {
  return apiClient.post(`/api/v1/tenants/${tenantId}/customers`, payload, token);
}

export function fetchCustomer(tenantId, customerId, token) {
  return apiClient.get(`/api/v1/tenants/${tenantId}/customers/${customerId}`, token);
}

export function updateCustomer(tenantId, customerId, payload, token) {
  return apiClient.put(`/api/v1/tenants/${tenantId}/customers/${customerId}`, payload, token);
}
