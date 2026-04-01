import { defineComponent } from "vue";
import template from "./template.html?raw";

export default defineComponent({
  name: "BaseToggleSwitch",
  inheritAttrs: false,
  props: {
    modelValue: {
      type: Boolean,
      default: false
    }
  },
  emits: ["update:modelValue"],
  template
});
