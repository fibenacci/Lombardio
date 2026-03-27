import { computed, defineComponent } from "vue";
import { RouterLink, RouterView, useRouter } from "vue-router";
import { useAuthStore } from "../../stores/auth";
import { useTenantStore } from "../../stores/tenant";
import { useI18n } from "../../i18n";
import template from "./template.html?raw";
import "./styles.scss";

export default defineComponent({
  name: "PlatformLayout",
  components: { RouterLink, RouterView },
  setup() {
    const router = useRouter();
    const { t } = useI18n();
    const authStore = useAuthStore();
    const tenantStore = useTenantStore();

    const user = computed(() => authStore.currentUser);
    const tenants = computed(() => tenantStore.tenants);

    async function impersonate(userId) {
      // Logic for impersonation if needed
    }

    async function logout() {
      await authStore.logout();
      router.push({ name: "login" });
    }

    return {
      impersonate,
      logout,
      t,
      tenants,
      user
    };
  },
  template
});
