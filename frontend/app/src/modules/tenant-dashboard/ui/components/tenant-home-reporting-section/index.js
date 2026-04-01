import { createDetailSectionComponent } from "../../../../../shared/ui/base/detail-section/create-detail-section";
import template from "./template.html?raw";
import "./styles.scss";

export default createDetailSectionComponent({
  name: "TenantHomeReportingSection",
  props: {
    financeTrendMax: { type: Number, default: 1 },
    formatCurrency: { type: Function, required: true },
    formatDate: { type: Function, required: true },
    getTransactionTypeLabel: { type: Function, required: true },
    inventoryMax: { type: Number, default: 1 },
    reportingError: { type: String, default: "" },
    reportingOverview: { type: Object, default: null },
    t: { type: Function, required: true }
  },
  template
});
