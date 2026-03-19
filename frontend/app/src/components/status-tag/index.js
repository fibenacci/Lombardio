import { computed, defineComponent } from "vue";
import template from "./template.html?raw";

export default defineComponent({
  name: "StatusTag",
  props: {
    value: {
      type: String,
      required: true
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
      if (props.activeValues.includes(props.value)) {
        return "success";
      }

      if (props.warnValues.includes(props.value)) {
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
