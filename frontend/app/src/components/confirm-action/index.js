import { defineComponent, ref } from "vue";
import template from "./template.html?raw";

export default defineComponent({
  name: "ConfirmAction",
  props: {
    label: {
      type: String,
      default: "Confirm"
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
    size: {
      type: String,
      default: null
    },
    header: {
      type: String,
      default: "Please confirm"
    },
    message: {
      type: String,
      default: ""
    },
    confirmLabel: {
      type: String,
      default: "Continue"
    },
    cancelLabel: {
      type: String,
      default: "Cancel"
    }
  },
  emits: ["confirm"],
  setup(_, { emit }) {
    const isOpen = ref(false);

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
      open
    };
  },
  template
});
