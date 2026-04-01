import { createDetailSectionComponent } from "../../../../../shared/ui/base/detail-section/create-detail-section";
import template from "./template.html?raw";
import "./styles.scss";

export default createDetailSectionComponent({
  name: "CustomerLoansSection",
  props: {
    formatCurrency: { type: Function, required: true },
    formatDate: { type: Function, required: true },
    formatDateTime: { type: Function, required: true },
    loans: { type: Array, default: () => [] },
    t: { type: Function, required: true }
  },
  template
});
