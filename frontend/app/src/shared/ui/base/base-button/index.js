import { defineComponent } from "vue";
import template from "./template.html?raw";

export default defineComponent({
  name: "BaseButton",
  inheritAttrs: false,
  props: {
    disabled: {
      type: Boolean,
      default: false
    },
    label: {
      type: String,
      default: ""
    },
    severity: {
      type: String,
      default: undefined
    },
    type: {
      type: String,
      default: "button"
    },
    outlined: {
      type: Boolean,
      default: false
    }
  },
  emits: ["click"],
  template
});
