import { computed, ref, type Ref } from "vue";
import { fetchTenantHomeReportingOverview } from "../../../infrastructure/adapters/http-tenant-dashboard.adapter";
import { calculateFinanceTrendMax, calculateInventoryMax } from "../../../domain/mappers";
import { getRequestErrorMessage } from "../../../../../shared/kernel/errors/request-error";
import type { TenantHomeReportingOverviewDto } from "../../../infrastructure/dto/tenant-dashboard.dto";

export function useTenantDashboardReporting({
  tenantId,
  t
}: {
  tenantId: Ref<string>;
  t: (key: string, params?: Record<string, unknown>) => string;
}) {
  const reportingOverview = ref<TenantHomeReportingOverviewDto | null>(null);
  const reportingError = ref("");

  const financeTrendMax = computed(() => calculateFinanceTrendMax(reportingOverview.value));
  const inventoryMax = computed(() => calculateInventoryMax(reportingOverview.value));

  async function loadReportingOverview() {
    if (!tenantId.value) {
      reportingOverview.value = null;
      return;
    }

    try {
      reportingError.value = "";
      reportingOverview.value = await fetchTenantHomeReportingOverview(tenantId.value, 14);
    } catch (error) {
      reportingOverview.value = null;
      reportingError.value = getRequestErrorMessage(error, t("tenantHome.messages.reportingFailed"));
    }
  }

  return {
    reportingOverview,
    reportingError,
    financeTrendMax,
    inventoryMax,
    loadReportingOverview
  };
}
