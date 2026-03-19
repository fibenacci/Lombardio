import { createApp } from "vue";
import App from "./app";
import router from "./router";
import { authStore } from "./stores/auth";
import { tenantStore } from "./stores/tenant";

await authStore.initialize();

try {
  await tenantStore.initialize();
} catch (error) {
  console.error("Failed to initialize tenant store", error);
}

createApp(App).use(router).mount("#app");
