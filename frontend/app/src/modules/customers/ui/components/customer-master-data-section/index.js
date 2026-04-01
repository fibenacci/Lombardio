import { createDetailSectionComponent } from "../../../../../shared/ui/base/detail-section/create-detail-section";
import template from "./template.html?raw";
import "./styles.scss";

export default createDetailSectionComponent({
  name: "CustomerMasterDataSection",
  props: {
    customer: { type: Object, required: true },
    isSavingCustomer: { type: Boolean, default: false },
    saveCustomer: { type: Function, required: true },
    t: { type: Function, required: true }
  },
  template
});
