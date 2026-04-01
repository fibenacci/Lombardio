import { defineComponent } from "vue";
import template from "./template.html?raw";
import "./styles.scss";

export default defineComponent({
  name: "AdminPanel",
  props: {
    title: {
      type: String,
      default: ""
    },
    subtitle: {
      type: String,
      default: ""
    }
  },
  template
});
