import { defineComponent } from "vue";
import { useRouter } from "vue-router";
import { useCustomerPortalStore } from "../../../../../app/session/state/customer-portal.store";
import { useI18n } from "../../../../../app/i18n";
import { useCustomerPortalLoginPage } from "../../composables/use-customer-portal-login-page";
import template from "./template.html?raw";
import "./styles.scss";

export default defineComponent({
  name: "CustomerPortalLoginPage",
  setup() {
    const customerPortalStore = useCustomerPortalStore();
    const router = useRouter();
    const { t } = useI18n();
    const page = useCustomerPortalLoginPage({
      customerPortalStore,
      router,
      t
    });

    return {
      ...page,
      t
    };
  },
  template
});
