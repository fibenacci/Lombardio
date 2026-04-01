import { defineComponent } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "../../../state/auth.store";
import { useTenantStore } from "../../../../tenant-context/state/tenant.store";
import { useI18n } from "../../../../../app/i18n";
import { useLoginPage } from "../../composables/use-login-page";
import template from "./template.html?raw";
import "./styles.scss";

export default defineComponent({
  name: "LoginPage",
  setup() {
    const router = useRouter();
    const authStore = useAuthStore();
    const tenantStore = useTenantStore();
    const { t } = useI18n();
    const page = useLoginPage({
      authStore,
      router,
      t,
      tenantStore
    });

    return {
      ...page,
      t
    };
  },
  template
});
