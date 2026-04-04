import { defineComponent, onMounted } from "vue";
import { useTenantStore } from "../../../../../app/tenant-context/state/tenant.store";
import { useI18n } from "../../../../../app/i18n";
import { useFormatters } from "../../../../../shared/kernel/utils/formatters";
import { useAuctionsPage } from "../../composables/use-auctions-page";
import template from "./template.html?raw";
import "./styles.scss";

export default defineComponent({
  name: "AuctionsPage",
  setup() {
    const tenantStore = useTenantStore();
    const { t } = useI18n();
    const { formatCurrency, formatDate } = useFormatters();
    const auctionsPage = useAuctionsPage({
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
