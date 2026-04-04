import { defineComponent, onMounted } from "vue";
import { useTenantStore } from "../../../../../app/tenant-context/state/tenant.store";
import { useI18n } from "../../../../../app/i18n";
import { useFormatters } from "../../../../../shared/kernel/utils/formatters";
import { useOnlineAuctionsPage } from "../../composables/use-online-auctions-page";
import template from "./template.html?raw";
import "./styles.scss";

export default defineComponent({
  name: "OnlineAuctionsPage",
  setup() {
    const tenantStore = useTenantStore();
    const { t } = useI18n();
    const { formatCurrency } = useFormatters();
    const onlineAuctionsPage = useOnlineAuctionsPage({
      t,
      tenantStore
    });

    onMounted(onlineAuctionsPage.reloadData);

    return {
      ...onlineAuctionsPage,
      formatCurrency,
      t
    };
  },
  template
});
