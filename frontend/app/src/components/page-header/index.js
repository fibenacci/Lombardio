import { defineComponent } from "vue";
import template from "./template.html?raw";
import "./styles.scss";

export default defineComponent({
  name: "PageHeader",
  props: {
    eyebrow: {
      type: String,
      default: ""
    },
    title: {
      type: String,
      default: ""
    },
    copy: {
      type: String,
      default: ""
    },
    icon: {
      type: String,
      default: "pi pi-sparkles"
    }
  },
  template
});
