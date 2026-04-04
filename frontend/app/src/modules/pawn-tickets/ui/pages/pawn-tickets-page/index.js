import { computed, defineComponent, onMounted } from "vue";
import { useAppToast } from "../../../../../shared/ui/composables/use-app-toast";
import { useTenantStore } from "../../../../../app/tenant-context/state/tenant.store";
import { useI18n } from "../../../../../app/i18n";
import { useFormatters } from "../../../../../shared/kernel/utils/formatters";
import { usePawnTicketsPage } from "../../composables/use-pawn-tickets-page";
import template from "./template.html?raw";
import "./styles.scss";

export default defineComponent({
  name: "PawnTicketsPage",
  setup() {
    const tenantStore = useTenantStore();
    const { t } = useI18n();
    const { formatCurrency, formatDate } = useFormatters();
    const toast = useAppToast();
    const pawnTicketsPage = usePawnTicketsPage({
      t,
      tenantStore,
      toast
    });
    const statusLabels = {
      ACTIVE: () => t("pawnTickets.status.ACTIVE"),
      DUE: () => t("pawnTickets.status.DUE"),
      REDEEMED: () => t("pawnTickets.status.REDEEMED"),
      EXTENDED: () => t("pawnTickets.status.EXTENDED"),
      AUCTIONED: () => t("pawnTickets.status.AUCTIONED")
    };
    const pageCopy = computed(() => {
      if (!tenantStore.selectedTenantId) {
        return t("pawnTickets.copyWithoutTenant");
      }

      const tenantDisplayName = tenantStore.selectedTenant?.displayName || tenantStore.selectedTenantId;
      return t("pawnTickets.copyWithTenant", { tenant: tenantDisplayName });
    });

    onMounted(pawnTicketsPage.loadData);

    return {
      ...pawnTicketsPage,
      formatCurrency,
      formatDate,
      getTicketStatusLabel: (status) => statusLabels[status]?.() ?? status ?? t("common.notAvailable"),
      pageCopy,
      t,
      tenantStore
    };
  },
  template
});
