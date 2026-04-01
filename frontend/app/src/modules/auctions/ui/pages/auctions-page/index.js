import { defineComponent, onMounted } from "vue";
import { useAuthStore } from "../../../../../app/session/state/auth.store";
import { useTenantStore } from "../../../../../app/tenant-context/state/tenant.store";
import { useI18n } from "../../../../../app/i18n";
import { useFormatters } from "../../../../../shared/kernel/utils/formatters";
import { useAuctionsPage } from "../../composables/use-auctions-page";
import template from "./template.html?raw";
import "./styles.scss";

export default defineComponent({
  name: "AuctionsPage",
  setup() {
    const authStore = useAuthStore();
    const tenantStore = useTenantStore();
    const { t } = useI18n();
    const { formatCurrency, formatDate } = useFormatters();
    const auctionsPage = useAuctionsPage({
      authStore,
      t,
      tenantStore
    });

    onMounted(auctionsPage.reloadData);

    return {
      ...auctionsPage,
      formatCurrency,
      formatDate,
      t
    };
  },
  template
});
