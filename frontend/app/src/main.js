import { createApp } from "vue";
import { createPinia } from "pinia";
import PrimeVue from "primevue/config";
import Aura from "@primeuix/themes/aura";
import ToastService from "primevue/toastservice";
import AutoComplete from "primevue/autocomplete";
import Button from "primevue/button";
import Card from "primevue/card";
import Column from "primevue/column";
import DataTable from "primevue/datatable";
import Dialog from "primevue/dialog";
import Divider from "primevue/divider";
import InputText from "primevue/inputtext";
import Message from "primevue/message";
import MultiSelect from "primevue/multiselect";
import Password from "primevue/password";
import ProgressSpinner from "primevue/progressspinner";
import Select from "primevue/select";
import Tag from "primevue/tag";
import Textarea from "primevue/textarea";
import Toast from "primevue/toast";
import ToggleSwitch from "primevue/toggleswitch";
import Toolbar from "primevue/toolbar";
import Avatar from "primevue/avatar";
import "primeicons/primeicons.css";
import App from "./app";
import router from "./router";
import AdminPanel from "./components/admin-panel";
import AppDataTable from "./components/app-data-table";
import ConfirmAction from "./components/confirm-action";
import FilterBar from "./components/filter-bar";
import FormFeedback from "./components/form-feedback";
import FormShell from "./components/form-shell";
import PageHeader from "./components/page-header";
import SectionActions from "./components/section-actions";
import StatusTag from "./components/status-tag";
import { useAuthStore } from "./stores/auth";
import { useCustomerPortalStore } from "./stores/customerPortal";
import { useTenantStore } from "./stores/tenant";

async function bootstrap() {
  console.log("[Bootstrap] Starting...");
  const app = createApp(App);
  const pinia = createPinia();
  app.use(pinia);

  app.use(PrimeVue, {
    theme: {
      preset: Aura,
      options: {
        darkModeSelector: false
      }
    }
  });
  app.use(ToastService);

  // Core PrimeVue Components
  app.component("PAutoComplete", AutoComplete);
  app.component("PAvatar", Avatar);
  app.component("PButton", Button);
  app.component("PCard", Card);
  app.component("PColumn", Column);
  app.component("PDataTable", DataTable);
  app.component("PDialog", Dialog);
  app.component("PDivider", Divider);
  app.component("PInputText", InputText);
  app.component("PMessage", Message);
  app.component("PMultiSelect", MultiSelect);
  app.component("PPassword", Password);
  app.component("PProgressSpinner", ProgressSpinner);
  app.component("PSelect", Select);
  app.component("PTag", Tag);
  app.component("PTextarea", Textarea);
  app.component("PToast", Toast);
  app.component("PToggleSwitch", ToggleSwitch);
  app.component("PToolbar", Toolbar);

  // App Components
  app.component("AdminPanel", AdminPanel);
  app.component("AppDataTable", AppDataTable);
  app.component("ConfirmAction", ConfirmAction);
  app.component("FilterBar", FilterBar);
  app.component("FormFeedback", FormFeedback);
  app.component("FormShell", FormShell);
  app.component("PageHeader", PageHeader);
  app.component("SectionActions", SectionActions);
  app.component("StatusTag", StatusTag);

  app.use(router);

  // IMPORTANT: Mount immediately to prevent white screen
  app.mount("#app");
  console.log("[Bootstrap] App mounted.");

  // Load state in background
  try {
    const authStore = useAuthStore();
    const customerPortalStore = useCustomerPortalStore();
    const tenantStore = useTenantStore();

    console.log("[Bootstrap] Initializing stores...");
    await authStore.initialize();
    await customerPortalStore.initialize();

    if (authStore.isAuthenticated) {
      await tenantStore.initialize();
    }
    console.log("[Bootstrap] Stores initialized.");
  } catch (error) {
    console.error("[Bootstrap] Initialization failed", error);
  }
}

bootstrap().catch((error) => {
  console.error("[Bootstrap] Fatal error", error);
});
