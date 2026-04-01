import { defineComponent } from "vue";
import template from "./template.html?raw";

export default defineComponent({
  name: "BaseInputText",
  inheritAttrs: false,
  props: {
    modelValue: {
      type: [String, Number],
      default: ""
    }
  },
  emits: ["update:modelValue"],
  template
});
