import { computed, defineComponent } from "vue";
import { RouterLink, RouterView, useRoute, useRouter } from "vue-router";
import { authStore } from "../../stores/auth";
import { useI18n } from "../../i18n";
import { tenantStore } from "../../stores/tenant";
import template from "./template.html?raw";
import "./styles.scss";

export default defineComponent({
  name: "PlatformLayout",
  components: { RouterLink, RouterView },
  setup() {
    const route = useRoute();
    const router = useRouter();
    const { t } = useI18n();
    const user = computed(() => authStore.user);
    const isImpersonating = computed(() => authStore.isImpersonating());
    const navigationItems = computed(() => [
      { name: "platform-tenants", to: "/platform/tenants", label: t("platformLayout.tenants"), icon: "pi pi-building" },
      { name: "platform-security", to: "/platform/security", label: t("platformLayout.security"), icon: "pi pi-shield" },
      { name: "tenant-home", to: "/app/dashboard", label: t("platformLayout.tenantApp"), icon: "pi pi-th-large" }
    ]);

    async function endDelegation() {
      await authStore.endDelegation();
      await tenantStore.refreshTenants();
      router.push({ name: "platform-tenants" });
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
      user
    };
  },
  template
});
