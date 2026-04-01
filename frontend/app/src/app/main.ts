import { createApp } from "vue";
import "primeicons/primeicons.css";
import App from "./App.vue";
import router from "./router";
import { createPiniaProvider } from "./providers/pinia";
import { installPrimeVue } from "./providers/primevue";
import { useAuthStore } from "./session/state/auth.store";
import { useCustomerPortalStore } from "./session/state/customer-portal.store";
import { useTenantStore } from "./tenant-context/state/tenant.store";

export async function bootstrapApp() {
  const app = createApp(App);
  const pinia = createPiniaProvider();

  app.use(pinia);

  try {
    const authStore = useAuthStore();
    const customerPortalStore = useCustomerPortalStore();
    const tenantStore = useTenantStore();

    await authStore.initialize();
    await customerPortalStore.initialize();

    if (authStore.isAuthenticated) {
      await tenantStore.initialize();
    }
  } catch (error) {
    console.error("[Bootstrap] Initialization failed", error);
  }

  installPrimeVue(app);
  app.use(router);
  await router.isReady();
  app.mount("#app");
}
