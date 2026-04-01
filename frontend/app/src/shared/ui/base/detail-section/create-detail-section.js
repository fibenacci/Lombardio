import { defineComponent } from "vue";
import DetailSection from ".";

export function createDetailSectionComponent({
  components = {},
  name,
  props = {},
  setup,
  template
}) {
  return defineComponent({
    name,
    components: {
      DetailSection,
      ...components
    },
    props: {
      title: {
        type: String,
        required: true
      },
      subtitle: {
        type: String,
        default: ""
      },
      ...props
    },
    setup,
    template
  });
}
