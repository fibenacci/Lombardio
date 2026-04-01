import { defineComponent } from "vue";
import template from "./template.html?raw";

export default defineComponent({
  name: "BaseAutoComplete",
  inheritAttrs: false,
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
      default: undefined
    },
    dropdown: {
      type: Boolean,
      default: false
    },
    forceSelection: {
      type: Boolean,
      default: false
    }
  },
  emits: ["update:modelValue", "complete", "item-select", "clear"],
  template
});
