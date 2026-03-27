import { defineComponent } from "vue";
import { useAuthStore } from "../../stores/auth";
import { useTenantStore } from "../../stores/tenant";
import template from "./template.html?raw";
import "./styles.scss";

export default defineComponent({
  name: "DashboardLayout",
  setup() {
    const authStore = useAuthStore();
    const tenantStore = useTenantStore();
    
    return {
      authStore,
      tenantStore
    };
  },
  template
});
