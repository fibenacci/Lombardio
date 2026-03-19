import { computed, defineComponent } from "vue";
import { RouterLink, RouterView, useRoute, useRouter } from "vue-router";
import { authStore } from "../../stores/auth";
import { tenantStore } from "../../stores/tenant";
import template from "./template.html?raw";
import "./styles.scss";

export default defineComponent({
  name: "DashboardLayout",
  components: {
    RouterLink,
    RouterView
  },
  setup() {
    const route = useRoute();
    const router = useRouter();
    const user = computed(() => authStore.user);
    const canManagePlatform = computed(() => authStore.canManagePlatform());
    const isImpersonating = computed(() => authStore.isImpersonating());

    async function endDelegation() {
      await authStore.endDelegation();
      await tenantStore.refreshTenants();
      router.push({ path: canManagePlatform.value ? "/tenants" : "/users" });
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
      user
    };
  },
  template
});
