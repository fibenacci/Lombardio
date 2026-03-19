import { customerGet, customerPost, customerPut } from "./client";

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
  const payload = await customerGet(`/api/v1/tenants/${tenantId}/customers?query=${encodeURIComponent(query ?? "")}`, token);
  return normalizeCustomerCollection(payload);
}

export function createCustomer(tenantId, payload, token) {
  return customerPost(`/api/v1/tenants/${tenantId}/customers`, payload, token);
}

export function fetchCustomer(tenantId, customerId, token) {
  return customerGet(`/api/v1/tenants/${tenantId}/customers/${customerId}`, token);
}

export function updateCustomer(tenantId, customerId, payload, token) {
  return customerPut(`/api/v1/tenants/${tenantId}/customers/${customerId}`, payload, token);
}
