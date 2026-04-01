import { BASE_URLS, createApiClient } from "./client";

const apiClient = createApiClient(BASE_URLS.reporting);

export function fetchDashboardOverview(tenantId, token, rangeDays = 14) {
  return apiClient.get(`/api/v1/tenants/${tenantId}/reporting/dashboard?rangeDays=${rangeDays}`, token);
}
