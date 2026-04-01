import { createDetailSectionComponent } from "../../../../../shared/ui/base/detail-section/create-detail-section";
import template from "./template.html?raw";
import "./styles.scss";

export default createDetailSectionComponent({
  name: "CustomerKycSection",
  props: {
    canPrefillDocument: { type: Boolean, default: false },
    clearDocument: { type: Function, required: true },
    documentOcrAvailable: { type: Boolean, default: false },
    isPrefillingKyc: { type: Boolean, default: false },
    isSavingKyc: { type: Boolean, default: false },
    kyc: { type: Object, required: true },
    kycDocumentTypeOptions: { type: Array, default: () => [] },
    kycStatusOptions: { type: Array, default: () => [] },
    prefillDocument: { type: Function, required: true },
    resolveDocumentImageSrc: { type: Function, required: true },
    saveKyc: { type: Function, required: true },
    t: { type: Function, required: true },
    updateDocument: { type: Function, required: true }
  },
  template
});
