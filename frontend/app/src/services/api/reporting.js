import { reportingGet } from "./client";

export function fetchDashboardOverview(tenantId, token, rangeDays = 14) {
  return reportingGet(`/api/v1/tenants/${tenantId}/reporting/dashboard?rangeDays=${rangeDays}`, token);
}
