import { defineComponent, onMounted } from "vue";
import { useRoute } from "vue-router";
import { useI18n } from "../../../../../app/i18n";
import { useFormatters } from "../../../../../shared/kernel/utils/formatters";
import { usePublicAuctionPage } from "../../composables/use-public-auction-page";
import template from "./template.html?raw";
import "./styles.scss";

export default defineComponent({
  name: "PublicAuctionPage",
  setup() {
    const route = useRoute();
    const { t } = useI18n();
    const { formatCurrency, formatDateTime } = useFormatters();
    const page = usePublicAuctionPage({
      route,
      t
    });

    onMounted(page.reloadAuction);

    return {
      ...page,
      formatCurrency,
      formatDateTime,
      t
    };
  },
  template
});
