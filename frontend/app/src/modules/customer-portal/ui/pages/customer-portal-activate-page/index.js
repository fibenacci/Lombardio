import { defineComponent, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useCustomerPortalStore } from "../../../../../app/session/state/customer-portal.store";
import { useI18n } from "../../../../../app/i18n";
import { useCustomerPortalActivatePage } from "../../composables/use-customer-portal-activate-page";
import template from "./template.html?raw";
import "./styles.scss";

export default defineComponent({
  name: "CustomerPortalActivatePage",
  setup() {
    const customerPortalStore = useCustomerPortalStore();
    const route = useRoute();
    const router = useRouter();
    const { t } = useI18n();
    const page = useCustomerPortalActivatePage({
      customerPortalStore,
      route,
      router,
      t
    });

    onMounted(page.loadInvitation);

    return {
      ...page,
      t
    };
  },
  template
});
