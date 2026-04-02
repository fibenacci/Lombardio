import { BASE_URLS, createApiClient } from "../../../../shared/kernel/http/runtime-api-client";

const apiClient = createApiClient(BASE_URLS.platform);

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

export async function searchCustomers(tenantId, query) {
  const payload = await apiClient.get(
    `/api/v1/platform/operator/tenants/${tenantId}/customers?query=${encodeURIComponent(query ?? "")}`
  );
  return normalizeCustomerCollection(payload);
}

export function createCustomer(tenantId, payload) {
  return apiClient.post(`/api/v1/platform/operator/tenants/${tenantId}/customers`, payload);
}

export function fetchCustomer(tenantId, customerId) {
  return apiClient.get(`/api/v1/platform/operator/tenants/${tenantId}/customers/${customerId}`);
}

export function updateCustomer(tenantId, customerId, payload) {
  return apiClient.put(`/api/v1/platform/operator/tenants/${tenantId}/customers/${customerId}`, payload);
}
