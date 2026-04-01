import { defineComponent } from "vue";

export default defineComponent({
  name: "AppLoadingState",
  props: {
    copy: {
      type: String,
      default: ""
    }
  },
  template: `
    <div class="panel">
      <p class="placeholder-copy">{{ copy }}</p>
    </div>
  `
});
