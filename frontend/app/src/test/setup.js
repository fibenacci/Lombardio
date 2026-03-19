import { afterEach, beforeEach, vi } from "vitest";
import { config } from "@vue/test-utils";
import { computed, defineComponent, h } from "vue";
import PrimeVue from "primevue/config";
import Aura from "@primeuix/themes/aura";
import ToastService from "primevue/toastservice";
import Avatar from "primevue/avatar";
import Button from "primevue/button";
import Card from "primevue/card";
import Column from "primevue/column";
import DataTable from "primevue/datatable";
import Dialog from "primevue/dialog";
import Divider from "primevue/divider";
import InputText from "primevue/inputtext";
import Message from "primevue/message";
import Password from "primevue/password";
import ProgressSpinner from "primevue/progressspinner";
import Tag from "primevue/tag";
import Textarea from "primevue/textarea";
import Toast from "primevue/toast";
import ToggleSwitch from "primevue/toggleswitch";
import Toolbar from "primevue/toolbar";
import AdminPanel from "../components/admin-panel";
import AppDataTable from "../components/app-data-table";
import ConfirmAction from "../components/confirm-action";
import FilterBar from "../components/filter-bar";
import FormFeedback from "../components/form-feedback";
import FormShell from "../components/form-shell";
import PageHeader from "../components/page-header";
import SectionActions from "../components/section-actions";
import StatusTag from "../components/status-tag";
import { authStore } from "../stores/auth";
import { customerPortalStore } from "../stores/customerPortal";
import { tenantStore } from "../stores/tenant";

const resolveOptions = (props) => props.options ?? [];

const normalizeOption = (option, props) => {
  if (option && typeof option === "object") {
    const label = props.optionLabel ? option[props.optionLabel] : (option.label ?? option.value ?? "");
    const value = props.optionValue ? option[props.optionValue] : (option.value ?? label);

    return {
      label,
      value
    };
  }

  return {
    label: String(option ?? ""),
    value: option
  };
};

const TestSelect = defineComponent({
  name: "TestPrimeSelect",
  props: {
    modelValue: {
      type: [String, Number, Boolean, Object, Array, null],
      default: null
    },
    options: {
      type: Array,
      default: () => []
    },
    optionLabel: {
      type: String,
      default: null
    },
    optionValue: {
      type: String,
      default: null
    }
  },
  emits: ["update:modelValue", "change"],
  setup(props, { emit, attrs }) {
    const normalizedOptions = computed(() => resolveOptions(props).map((option) => normalizeOption(option, props)));

    const onChange = (event) => {
      const selected = normalizedOptions.value.find((option) => String(option.value) === event.target.value);
      const value = selected ? selected.value : event.target.value;
      emit("update:modelValue", value);
      emit("change", { originalEvent: event, value });
    };

    return () => h(
      "select",
      {
        ...attrs,
        class: ["test-prime-select", attrs.class],
        value: props.modelValue ?? "",
        onChange
      },
      normalizedOptions.value.map((option) => h("option", { value: option.value }, option.label))
    );
  }
});

const TestMultiSelect = defineComponent({
  name: "TestPrimeMultiSelect",
  props: {
    modelValue: {
      type: Array,
      default: () => []
    },
    options: {
      type: Array,
      default: () => []
    },
    optionLabel: {
      type: String,
      default: null
    },
    optionValue: {
      type: String,
      default: null
    }
  },
  emits: ["update:modelValue", "change"],
  setup(props, { emit, attrs }) {
    const normalizedOptions = computed(() => resolveOptions(props).map((option) => normalizeOption(option, props)));

    const onChange = (event) => {
      const value = Array.from(event.target.selectedOptions, (option) => option.value);
      emit("update:modelValue", value);
      emit("change", { originalEvent: event, value });
    };

    return () => h(
      "select",
      {
        ...attrs,
        multiple: true,
        class: ["test-prime-multiselect", attrs.class],
        value: props.modelValue,
        onChange
      },
      normalizedOptions.value.map((option) => h("option", { value: option.value }, option.label))
    );
  }
});

config.global.plugins = [
  [PrimeVue, {
    theme: {
      preset: Aura,
      options: {
        darkModeSelector: false
      }
    }
  }],
  ToastService
];

config.global.components = {
  AdminPanel,
  AppDataTable,
  ConfirmAction,
  PAvatar: Avatar,
  PButton: Button,
  PCard: Card,
  PColumn: Column,
  PDataTable: DataTable,
  PDialog: Dialog,
  PDivider: Divider,
  PInputText: InputText,
  PMessage: Message,
  PMultiSelect: TestMultiSelect,
  PPassword: Password,
  PProgressSpinner: ProgressSpinner,
  PSelect: TestSelect,
  PTag: Tag,
  PTextarea: Textarea,
  PToast: Toast,
  PToggleSwitch: ToggleSwitch,
  PToolbar: Toolbar,
  FilterBar,
  FormFeedback,
  FormShell,
  PageHeader,
  SectionActions,
  StatusTag
};

const matchMediaMock = vi.fn().mockImplementation((query) => ({
  matches: false,
  media: query,
  onchange: null,
  addListener: vi.fn(),
  removeListener: vi.fn(),
  addEventListener: vi.fn(),
  removeEventListener: vi.fn(),
  dispatchEvent: vi.fn()
}));

const screenOrientationMock = {
  addEventListener: vi.fn(),
  removeEventListener: vi.fn()
};

beforeEach(() => {
  window.matchMedia = matchMediaMock;
  globalThis.matchMedia = matchMediaMock;
  Object.defineProperty(window.screen, "orientation", {
    configurable: true,
    value: screenOrientationMock
  });
  window.localStorage.clear();
  authStore.resetForTests();
  customerPortalStore.resetForTests();
  tenantStore.resetForTests();
});

afterEach(() => {
  vi.restoreAllMocks();
});
