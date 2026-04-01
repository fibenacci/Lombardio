import { computed, defineComponent } from "vue";
import { RouterLink, RouterView, useRoute, useRouter } from "vue-router";
import { useAuthStore } from "../../session/state/auth.store";
import { useTenantStore } from "../../tenant-context/state/tenant.store";
import { useI18n } from "../../i18n";
import template from "./template.html?raw";
import "./styles.scss";

export default defineComponent({
  name: "PlatformLayout",
  components: { RouterLink, RouterView },
  setup() {
    const route = useRoute();
    const router = useRouter();
    const { t } = useI18n();
    const authStore = useAuthStore();
    const tenantStore = useTenantStore();

    const user = computed(() => authStore.currentUser);
    const tenants = computed(() => tenantStore.tenants);
    const isImpersonating = computed(() => authStore.user?.impersonating || false);
    const navigationItems = computed(() => [
      {
        icon: "pi pi-building",
        label: t("platformLayout.tenants"),
        name: "platform-tenants",
        to: "/platform/tenants"
      },
      {
        icon: "pi pi-shield",
        label: t("platformLayout.security"),
        name: "platform-security",
        to: "/platform/security"
      },
      {
        icon: "pi pi-briefcase",
        label: t("platformLayout.tenantApp"),
        name: "tenant-home",
        to: "/app/dashboard"
      }
    ]);

    async function endDelegation() {
      return undefined;
    }

    async function logout() {
      await authStore.logout();
      router.push({ name: "login" });
    }

    return {
      endDelegation,
      isImpersonating,
      logout,
      navigationItems,
      route,
      t,
      tenants,
      user
    };
  },
  template
});
