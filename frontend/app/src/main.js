import { createApp } from "vue";
import PrimeVue from "primevue/config";
import Aura from "@primeuix/themes/aura";
import ToastService from "primevue/toastservice";
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
import { authStore } from "./stores/auth";
import { customerPortalStore } from "./stores/customerPortal";
import { tenantStore } from "./stores/tenant";

async function bootstrap() {
  await authStore.initialize();
  await customerPortalStore.initialize();

  try {
    await tenantStore.initialize();
  } catch (error) {
    console.error("Failed to initialize tenant store", error);
  }

  const app = createApp(App);

  app.use(PrimeVue, {
    theme: {
      preset: Aura,
      options: {
        darkModeSelector: false
      }
    }
  });
  app.use(ToastService);

  app.component("AdminPanel", AdminPanel);
  app.component("AppDataTable", AppDataTable);
  app.component("ConfirmAction", ConfirmAction);
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
  app.component("FilterBar", FilterBar);
  app.component("FormFeedback", FormFeedback);
  app.component("FormShell", FormShell);
  app.component("PageHeader", PageHeader);
  app.component("SectionActions", SectionActions);
  app.component("StatusTag", StatusTag);

  app.use(router).mount("#app");
}

bootstrap().catch((error) => {
  console.error("Failed to bootstrap application", error);
});
