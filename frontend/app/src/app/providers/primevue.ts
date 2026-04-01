import type { App } from "vue";
import PrimeVue from "primevue/config";
import Aura from "@primeuix/themes/aura";
import ToastService from "primevue/toastservice";
import AutoComplete from "primevue/autocomplete";
import Avatar from "primevue/avatar";
import Button from "primevue/button";
import Card from "primevue/card";
import Column from "primevue/column";
import DataTable from "primevue/datatable";
import Dialog from "primevue/dialog";
import Divider from "primevue/divider";
import FileUpload from "primevue/fileupload";
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
import {
  AdminPanel,
  AppDataTable,
  BaseAutoComplete,
  BaseButton,
  BaseFileUpload,
  BaseInputText,
  BaseSelect,
  BaseTextarea,
  BaseToggleSwitch,
  ConfirmAction,
  FilterBar,
  FormShell,
  PageHeader,
  SectionActions,
  StatusTag
} from "../../shared/ui/base";
import { FormFeedback } from "../../shared/ui/feedback";

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

  app.component("PAutoComplete", AutoComplete);
  app.component("PAvatar", Avatar);
  app.component("PButton", Button);
  app.component("PCard", Card);
  app.component("PColumn", Column);
  app.component("PDataTable", DataTable);
  app.component("PDialog", Dialog);
  app.component("PDivider", Divider);
  app.component("PFileUpload", FileUpload);
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

  app.component("AdminPanel", AdminPanel);
  app.component("AppDataTable", AppDataTable);
  app.component("BaseAutoComplete", BaseAutoComplete);
  app.component("BaseButton", BaseButton);
  app.component("BaseFileUpload", BaseFileUpload);
  app.component("BaseInputText", BaseInputText);
  app.component("BaseSelect", BaseSelect);
  app.component("BaseTextarea", BaseTextarea);
  app.component("BaseToggleSwitch", BaseToggleSwitch);
  app.component("ConfirmAction", ConfirmAction);
  app.component("FilterBar", FilterBar);
  app.component("FormFeedback", FormFeedback);
  app.component("FormShell", FormShell);
  app.component("PageHeader", PageHeader);
  app.component("SectionActions", SectionActions);
  app.component("StatusTag", StatusTag);
}
