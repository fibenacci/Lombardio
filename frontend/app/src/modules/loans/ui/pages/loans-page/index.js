import { computed, defineComponent, onMounted } from "vue";
import { useAuthStore } from "../../../../../app/session/state/auth.store";
import { useTenantStore } from "../../../../../app/tenant-context/state/tenant.store";
import { useI18n } from "../../../../../app/i18n";
import { useFormatters } from "../../../../../shared/kernel/utils/formatters";
import { useLoansPage } from "../../composables/use-loans-page";
import template from "./template.html?raw";
import "./styles.scss";

export default defineComponent({
  name: "LoansPage",
  setup() {
    const authStore = useAuthStore();
    const tenantStore = useTenantStore();
    const { t } = useI18n();
    const { formatCurrency, formatDate, formatDateTime } = useFormatters();
    const loansPage = useLoansPage({
      authStore,
      t,
      tenantStore
    });
    const pageCopy = computed(() => {
      if (!tenantStore.selectedTenantId) {
        return t("loans.copyWithoutTenant");
      }

      const tenantDisplayName = tenantStore.selectedTenant?.displayName || tenantStore.selectedTenantId;
      return t("loans.copyWithTenant", { tenant: tenantDisplayName });
    });

    onMounted(loansPage.loadData);

    return {
      ...loansPage,
      formatCurrency,
      formatDate,
      formatDateTime,
      pageCopy,
      t,
      tenantStore
    };
  },
  template
});
