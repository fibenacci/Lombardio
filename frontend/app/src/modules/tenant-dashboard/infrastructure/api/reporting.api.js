import { BASE_URLS, createApiClient } from "../../../../shared/kernel/http/runtime-api-client";

const apiClient = createApiClient(BASE_URLS.platform);

export function fetchDashboardOverview(tenantId, rangeDays = 14) {
  return apiClient.get(`/api/v1/platform/operator/tenants/${tenantId}/reporting/dashboard?rangeDays=${rangeDays}`);
}
