import { defineComponent } from "vue";
import { useI18n } from "../../../../../app/i18n";
import { useAuthStore } from "../../../../../app/session/state/auth.store";
import { useTenantStore } from "../../../../../app/tenant-context/state/tenant.store";
import { useUsersPage } from "../../composables/use-users-page";
import template from "./template.html?raw";
import "./styles.scss";

export default defineComponent({
  name: "UsersPage",
  setup() {
    const { t } = useI18n();
    const authStore = useAuthStore();
    const tenantStore = useTenantStore();
    
    const usersPage = useUsersPage({
      t,
      authStore,
      tenantStore
    });

    return {
      ...usersPage
    };
  },
  template
});
