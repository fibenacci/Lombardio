import { computed, defineComponent, onMounted } from "vue";
import { useTenantStore } from "../../../../../app/tenant-context/state/tenant.store";
import { useI18n } from "../../../../../app/i18n";
import { useFormatters } from "../../../../../shared/kernel/utils/formatters";
import { useTenantDashboardService } from "../../../application/services/use-tenant-dashboard.service";
import {
  TenantHomeOriginationWorkflow,
  TenantHomeReportingSection,
  TenantHomeTicketPanel
} from "../../components";
import template from "./template.html?raw";
import "./styles.scss";

export default defineComponent({
  name: "TenantDashboardPage",
  components: {
    TenantHomeOriginationWorkflow,
    TenantHomeReportingSection,
    TenantHomeTicketPanel
  },
  setup() {
    const { t } = useI18n();
    const { formatCurrency, formatDate } = useFormatters();
    const tenantStore = useTenantStore();
    const tenantHome = useTenantDashboardService({
      t,
      tenantStore
    });
    const pageTitle = computed(() => tenantStore.selectedTenant?.displayName || t("tenantHome.titleFallback"));

    onMounted(tenantHome.loadContext);

    return {
      ...tenantHome,
      formatCurrency,
      formatDate,
      pageTitle,
      t
    };
  },
  template
});
