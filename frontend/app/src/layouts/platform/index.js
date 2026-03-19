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
      route,
      t,
      user
    };
  },
  template
});
