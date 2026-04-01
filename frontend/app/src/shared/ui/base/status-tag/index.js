import { computed, defineComponent } from "vue";
import template from "./template.html?raw";

export default defineComponent({
  name: "StatusTag",
  props: {
    value: {
      type: String,
      default: "UNKNOWN"
    },
    activeValues: {
      type: Array,
      default: () => ["ACTIVE", "CLEAR", "APPROVED", "ENABLED"]
    },
    warnValues: {
      type: Array,
      default: () => ["PENDING", "REVIEW_REQUIRED"]
    }
  },
  setup(props) {
    const severity = computed(() => {
      const val = props.value || "UNKNOWN";

      if (props.activeValues.includes(val)) {
        return "success";
      }

      if (props.warnValues.includes(val)) {
        return "warn";
      }

      return "contrast";
    });

    return {
      severity
    };
  },
  template
});
