import { computed, defineComponent, ref } from "vue";
import { useI18n } from "../../../../app/i18n";
import template from "./template.html?raw";

export default defineComponent({
  name: "ConfirmAction",
  props: {
    label: {
      type: String,
      default: null
    },
    icon: {
      type: String,
      default: ""
    },
    severity: {
      type: String,
      default: "secondary"
    },
    outlined: {
      type: Boolean,
      default: false
    },
    rounded: {
      type: Boolean,
      default: false
    },
    variant: {
      type: String,
      default: null
    },
    size: {
      type: String,
      default: null
    },
    header: {
      type: String,
      default: null
    },
    message: {
      type: String,
      default: ""
    },
    confirmLabel: {
      type: String,
      default: null
    },
    cancelLabel: {
      type: String,
      default: null
    }
  },
  emits: ["confirm"],
  setup(props, { emit }) {
    const { t } = useI18n();
    const isOpen = ref(false);
    const resolvedLabel = computed(() => props.label ?? t("confirmAction.trigger"));
    const resolvedHeader = computed(() => props.header ?? t("confirmAction.header"));
    const resolvedConfirmLabel = computed(() => props.confirmLabel ?? t("confirmAction.confirm"));
    const resolvedCancelLabel = computed(() => props.cancelLabel ?? t("confirmAction.cancel"));

    function open() {
      isOpen.value = true;
    }

    function close() {
      isOpen.value = false;
    }

    function confirm() {
      emit("confirm");
      close();
    }

    return {
      close,
      confirm,
      isOpen,
      open,
      resolvedCancelLabel,
      resolvedConfirmLabel,
      resolvedHeader,
      resolvedLabel
    };
  },
  template
});
