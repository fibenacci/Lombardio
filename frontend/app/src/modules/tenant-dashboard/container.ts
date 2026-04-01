import * as adapter from "./infrastructure/adapters/http-tenant-dashboard.adapter";

export function createTenantDashboardContainer() {
  return {
    adapter
  };
}
