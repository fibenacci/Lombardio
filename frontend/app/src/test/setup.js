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
import { createPinia, setActivePinia } from "pinia";
import { useAuthStore } from "../stores/auth";
import { useCustomerPortalStore } from "../stores/customerPortal";
import { useTenantStore } from "../stores/tenant";
import * as platformApi from "../services/api/platform";

vi.mock("../services/api/platform", () => ({
  fetchTenants: vi.fn(() => Promise.resolve([])),
  fetchTenantFeatures: vi.fn(() => Promise.resolve([])),
  createTenant: vi.fn((data) => Promise.resolve({ id: "mock-id", ...data })),
  createTenantUser: vi.fn((tenantId, data) => Promise.resolve({ id: "mock-user-id", tenantId, ...data })),
  upsertTenantFeature: vi.fn(() => Promise.resolve({}))
}));

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

const TestAutoComplete = defineComponent({
  name: "TestPrimeAutoComplete",
  props: {
    modelValue: {
      type: [String, Number, Boolean, Object, Array, null],
      default: null
    },
    suggestions: {
      type: Array,
      default: () => []
    },
    optionLabel: {
      type: String,
      default: null
    }
  },
  emits: ["update:modelValue", "complete", "item-select", "clear"],
  setup(props, { emit, attrs }) {
    const inputValue = computed(() => {
      if (props.modelValue && typeof props.modelValue === "object") {
        return props.optionLabel ? props.modelValue[props.optionLabel] : (props.modelValue.label ?? "");
      }

      return String(props.modelValue ?? "");
    });

    const onInput = (event) => {
      emit("update:modelValue", event.target.value);
      emit("complete", { originalEvent: event, query: event.target.value });
    };

    const onChange = (event) => {
      const option = props.suggestions.find((entry) => {
        const label = props.optionLabel ? entry?.[props.optionLabel] : (entry?.label ?? entry?.value ?? "");
        return label === event.target.value;
      });

      if (option) {
        emit("update:modelValue", option);
        emit("item-select", { originalEvent: event, value: option });
        return;
      }

      emit("update:modelValue", event.target.value);
    };

    return () => h("input", {
      ...attrs,
      class: ["test-prime-autocomplete", attrs.class],
      value: inputValue.value,
      onInput,
      onChange
    });
  }
});

const TestFileUpload = defineComponent({
  name: "TestPrimeFileUpload",
  props: {
    multiple: {
      type: Boolean,
      default: false
    }
  },
  emits: ["select", "clear"],
  setup(props, { emit, slots, attrs }) {
    const chooseCallback = () => {};
    const clearCallback = () => emit("clear");

    return () => h("div", { ...attrs, class: ["test-prime-fileupload", attrs.class] }, [
      slots.header?.({ chooseCallback, clearCallback, files: [] }),
      slots.content?.({ files: [] }),
      slots.empty?.()
    ]);
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
  PAutoComplete: TestAutoComplete,
  PAvatar: Avatar,
  PButton: Button,
  PCard: Card,
  PColumn: Column,
  PDataTable: DataTable,
  PDialog: Dialog,
  PDivider: Divider,
  PFileUpload: TestFileUpload,
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
  setActivePinia(createPinia());
  window.matchMedia = matchMediaMock;
  globalThis.matchMedia = matchMediaMock;
  Object.defineProperty(window.screen, "orientation", {
    configurable: true,
    value: screenOrientationMock
  });
  window.localStorage.clear();
  
  const auth = useAuthStore();
  const customerPortal = useCustomerPortalStore();
  const tenant = useTenantStore();
  
  auth.resetForTests();
  customerPortal.resetForTests();
  tenant.resetForTests();
});

afterEach(() => {
  vi.restoreAllMocks();
});
