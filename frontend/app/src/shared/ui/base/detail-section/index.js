import { defineComponent } from "vue";
import template from "./template.html?raw";
import "./styles.scss";

export default defineComponent({
  name: "DetailSection",
  props: {
    title: {
      type: String,
      required: true
    },
    subtitle: {
      type: String,
      default: ""
    },
    panelClass: {
      type: [String, Array, Object],
      default: ""
    },
    headerClass: {
      type: [String, Array, Object],
      default: ""
    }
  },
  template
});
