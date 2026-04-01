import { defineComponent, onMounted } from "vue";
import { useRouter } from "vue-router";
import { useCustomerPortalStore } from "../../../../../app/session/state/customer-portal.store";
import { useI18n } from "../../../../../app/i18n";
import { useFormatters } from "../../../../../shared/kernel/utils/formatters";
import { useCustomerPortalHomePage } from "../../composables/use-customer-portal-home-page";
import template from "./template.html?raw";
import "./styles.scss";

export default defineComponent({
  name: "CustomerPortalHomePage",
  setup() {
    const customerPortalStore = useCustomerPortalStore();
    const router = useRouter();
    const { t } = useI18n();
    const { formatCurrency, formatDate } = useFormatters();
    const page = useCustomerPortalHomePage({
      customerPortalStore,
      router,
      t
    });

    onMounted(page.loadData);

    return {
      ...page,
      formatCurrency,
      formatDate,
      t
    };
  },
  template
});
