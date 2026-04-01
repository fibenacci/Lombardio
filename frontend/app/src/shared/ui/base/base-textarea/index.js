import { defineComponent } from "vue";
import template from "./template.html?raw";

export default defineComponent({
  name: "BaseTextarea",
  inheritAttrs: false,
  props: {
    modelValue: {
      type: String,
      default: ""
    }
  },
  emits: ["update:modelValue"],
  template
});
