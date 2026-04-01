import { createDetailSectionComponent } from "../../../../../shared/ui/base/detail-section/create-detail-section";
import template from "./template.html?raw";
import "./styles.scss";

export default createDetailSectionComponent({
  name: "CustomerAmlSection",
  props: {
    aml: { type: Object, required: true },
    amlRiskLevelOptions: { type: Array, default: () => [] },
    amlStatusOptions: { type: Array, default: () => [] },
    isSavingAml: { type: Boolean, default: false },
    saveAml: { type: Function, required: true },
    t: { type: Function, required: true }
  },
  template
});
