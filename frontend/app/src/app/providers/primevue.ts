import { defineAsyncComponent, type App } from "vue";
import PrimeVue from "primevue/config";
import Aura from "@primeuix/themes/aura";
import ToastService from "primevue/toastservice";
import Avatar from "primevue/avatar";
import Button from "primevue/button";
import Card from "primevue/card";
import Divider from "primevue/divider";
import InputText from "primevue/inputtext";
import Message from "primevue/message";
import Tag from "primevue/tag";
import Textarea from "primevue/textarea";
import Toast from "primevue/toast";
import Toolbar from "primevue/toolbar";

function registerAsyncPrimeComponent(app: App, name: string, loader: Parameters<typeof defineAsyncComponent>[0]) {
  app.component(name, defineAsyncComponent(loader));
}

function registerAsyncLocalComponent(app: App, name: string, loader: Parameters<typeof defineAsyncComponent>[0]) {
  app.component(name, defineAsyncComponent(loader));
}

export function installPrimeVue(app: App) {
  app.use(PrimeVue, {
    theme: {
      preset: Aura,
      options: {
        darkModeSelector: false
      }
    }
  });
  app.use(ToastService);

  app.component("PAvatar", Avatar);
  app.component("PButton", Button);
  app.component("PCard", Card);
  app.component("PDivider", Divider);
  app.component("PInputText", InputText);
  app.component("PMessage", Message);
  app.component("PTag", Tag);
  app.component("PTextarea", Textarea);
  app.component("PToast", Toast);
  app.component("PToolbar", Toolbar);
  registerAsyncPrimeComponent(app, "PAutoComplete", () => import("primevue/autocomplete"));
  registerAsyncPrimeComponent(app, "PColumn", () => import("primevue/column"));
  registerAsyncPrimeComponent(app, "PDataTable", () => import("primevue/datatable"));
  registerAsyncPrimeComponent(app, "PDialog", () => import("primevue/dialog"));
  registerAsyncPrimeComponent(app, "PFileUpload", () => import("primevue/fileupload"));
  registerAsyncPrimeComponent(app, "PMultiSelect", () => import("primevue/multiselect"));
  registerAsyncPrimeComponent(app, "PPassword", () => import("primevue/password"));
  registerAsyncPrimeComponent(app, "PProgressSpinner", () => import("primevue/progressspinner"));
  registerAsyncPrimeComponent(app, "PSelect", () => import("primevue/select"));
  registerAsyncPrimeComponent(app, "PToggleSwitch", () => import("primevue/toggleswitch"));

  registerAsyncLocalComponent(app, "AdminPanel", () => import("../../shared/ui/base/admin-panel"));
  registerAsyncLocalComponent(app, "AppDataTable", () => import("../../shared/ui/base/base-data-table"));
  registerAsyncLocalComponent(app, "BaseAutoComplete", () => import("../../shared/ui/base/base-auto-complete"));
  registerAsyncLocalComponent(app, "BaseButton", () => import("../../shared/ui/base/base-button"));
  registerAsyncLocalComponent(app, "BaseFileUpload", () => import("../../shared/ui/base/base-file-upload"));
  registerAsyncLocalComponent(app, "BaseInputText", () => import("../../shared/ui/base/base-input-text"));
  registerAsyncLocalComponent(app, "BaseSelect", () => import("../../shared/ui/base/base-select"));
  registerAsyncLocalComponent(app, "BaseTextarea", () => import("../../shared/ui/base/base-textarea"));
  registerAsyncLocalComponent(app, "BaseToggleSwitch", () => import("../../shared/ui/base/base-toggle-switch"));
  registerAsyncLocalComponent(app, "ConfirmAction", () => import("../../shared/ui/base/confirm-action"));
  registerAsyncLocalComponent(app, "FilterBar", () => import("../../shared/ui/base/filter-bar"));
  registerAsyncLocalComponent(app, "FormFeedback", () => import("../../shared/ui/feedback/form-feedback"));
  registerAsyncLocalComponent(app, "FormShell", () => import("../../shared/ui/base/form-shell"));
  registerAsyncLocalComponent(app, "PageHeader", () => import("../../shared/ui/base/base-page-header"));
  registerAsyncLocalComponent(app, "SectionActions", () => import("../../shared/ui/base/section-actions"));
  registerAsyncLocalComponent(app, "StatusTag", () => import("../../shared/ui/base/status-tag"));
}
