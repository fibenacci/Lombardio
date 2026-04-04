import { defineComponent } from "vue";
import { useI18n } from "../../../../../app/i18n";
import { useTenantStore } from "../../../../../app/tenant-context/state/tenant.store";
import { useTenantsPage } from "../../composables/use-tenants-page";
import template from "./template.html?raw";
import "./styles.scss";

export default defineComponent({
  name: "TenantsPage",
  setup() {
    const { t } = useI18n();
    const tenantStore = useTenantStore();
    
    const tenantsPage = useTenantsPage({
      t,
      tenantStore
    });

    return {
      ...tenantsPage
    };
  },
  template
});
