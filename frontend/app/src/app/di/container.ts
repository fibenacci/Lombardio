import { createCustomerDetailContainer } from "../../modules/customers/container";
import { createTenantDashboardContainer } from "../../modules/tenant-dashboard/container";

export function createAppContainer() {
  return {
    customers: createCustomerDetailContainer(),
    tenantDashboard: createTenantDashboardContainer()
  };
}
