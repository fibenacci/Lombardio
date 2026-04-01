import { computed, defineComponent } from "vue";
import { RouterLink, RouterView, useRoute, useRouter } from "vue-router";
import { useAuthStore } from "../../session/state/auth.store";
import { useI18n } from "../../i18n";
import template from "./template.html?raw";
import "./styles.scss";

export default defineComponent({
  name: "DashboardLayout",
  components: { RouterLink, RouterView },
  setup() {
    const route = useRoute();
    const router = useRouter();
    const { t } = useI18n();
    const authStore = useAuthStore();

    const user = computed(() => authStore.currentUser);
    const canManagePlatform = computed(() => authStore.canManagePlatform);
    const isImpersonating = computed(() => authStore.user?.impersonating || false);

    async function endDelegation() {
      return undefined;
    }

    async function logout() {
      await authStore.logout();
      router.push({ name: "login" });
    }

    return {
      canManagePlatform,
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
