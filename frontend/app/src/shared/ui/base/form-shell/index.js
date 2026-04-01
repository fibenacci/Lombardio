import { defineComponent } from "vue";
import template from "./template.html?raw";

export default defineComponent({
  name: "FormShell",
  props: {
    errorMessage: {
      type: String,
      default: ""
    },
    successMessage: {
      type: String,
      default: ""
    }
  },
  emits: ["submit"],
  template
});
