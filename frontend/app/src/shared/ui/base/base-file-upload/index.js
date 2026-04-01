import { defineComponent } from "vue";
import template from "./template.html?raw";

export default defineComponent({
  name: "BaseFileUpload",
  inheritAttrs: false,
  props: {
    accept: {
      type: String,
      default: undefined
    },
    customUpload: {
      type: Boolean,
      default: false
    },
    mode: {
      type: String,
      default: "advanced"
    },
    multiple: {
      type: Boolean,
      default: false
    },
    showCancelButton: {
      type: Boolean,
      default: true
    },
    showUploadButton: {
      type: Boolean,
      default: true
    }
  },
  emits: ["select", "clear"],
  template
});
