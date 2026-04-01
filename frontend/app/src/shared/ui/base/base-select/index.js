import { defineComponent } from "vue";
import template from "./template.html?raw";

export default defineComponent({
  name: "BaseSelect",
  inheritAttrs: false,
  props: {
    modelValue: {
      type: [String, Number, Boolean, Object, Array, null],
      default: null
    }
  },
  emits: ["change", "update:modelValue"],
  template
});
