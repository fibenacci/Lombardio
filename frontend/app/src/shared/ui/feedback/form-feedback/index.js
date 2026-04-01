import { computed, defineComponent } from "vue";
import template from "./template.html?raw";
import "./styles.scss";

export default defineComponent({
  name: "FormFeedback",
  props: {
    message: {
      type: String,
      default: ""
    },
    fieldErrors: {
      type: Array,
      default: () => []
    }
  },
  setup(props) {
    const normalizedFieldErrors = computed(() =>
      props.fieldErrors.map((fieldError) => ({
        field: formatFieldLabel(fieldError.field),
        message: fieldError.message
      }))
    );

    return {
      normalizedFieldErrors
    };
  },
  template
});

function formatFieldLabel(field) {
  return String(field ?? "")
    .replace(/\[(\d+)\]/g, " $1 ")
    .replace(/\./g, " ")
    .replace(/([a-z])([A-Z])/g, "$1 $2")
    .replace(/\s+/g, " ")
    .trim();
}
