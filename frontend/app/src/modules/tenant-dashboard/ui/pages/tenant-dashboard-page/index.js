import { defineComponent, onMounted } from "vue";
import { useAuthStore } from "../../../../../app/session/state/auth.store";
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
    const authStore = useAuthStore();
    const tenantStore = useTenantStore();
    const tenantHome = useTenantDashboardService({
      authStore,
      t,
      tenantStore
    });

    onMounted(tenantHome.loadContext);

    return {
      ...tenantHome,
      formatCurrency,
      formatDate,
      t
    };
  },
  template
});
