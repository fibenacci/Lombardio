import { createDetailSectionComponent } from "../../../../../shared/ui/base/detail-section/create-detail-section";
import template from "./template.html?raw";
import "./styles.scss";

export default createDetailSectionComponent({
  name: "TenantHomeTicketPanel",
  props: {
    createdLoan: { type: Object, default: null },
    formatCurrency: { type: Function, required: true },
    formatDate: { type: Function, required: true },
    isDownloadingTicket: { type: Boolean, default: false },
    loanQuotes: { type: Array, default: () => [] },
    openPawnTicketDocument: { type: Function, required: true },
    openPawnTicketLabels: { type: Function, required: true },
    t: { type: Function, required: true }
  },
  template
});
