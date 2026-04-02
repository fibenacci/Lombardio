import { computed } from "vue";
import { FormFeedback } from "../../../../../shared/ui/feedback";
import { createDetailSectionComponent } from "../../../../../shared/ui/base/detail-section/create-detail-section";
import template from "./template.html?raw";
import "./styles.scss";

export default createDetailSectionComponent({
  name: "TenantHomeOriginationWorkflow",
  components: {
    FormFeedback
  },
  props: {
    addPosition: { type: Function, required: true },
    amlFeatureEnabled: { type: Boolean, default: false },
    amlRiskLevelOptions: { type: Array, default: () => [] },
    amlStatusOptions: { type: Array, default: () => [] },
    applyGuideline: { type: Function, required: true },
    approveSelectedCustomerKyc: { type: Function, required: true },
    canPrefillNewCustomerDocument: { type: Boolean, default: false },
    canSubmitLoan: { type: Boolean, default: false },
    clearNewCustomerDocument: { type: Function, required: true },
    clearPowerOfAttorneyDocument: { type: Function, required: true },
    customerSuggestions: { type: Array, default: () => [] },
    documentOcrAvailable: { type: Boolean, default: false },
    documentTypeOptions: { type: Array, default: () => [] },
    errorMessage: { type: String, default: "" },
    fieldErrors: { type: Array, default: () => [] },
    formatCurrency: { type: Function, required: true },
    getAmlStatusLabel: { type: Function, required: true },
    getKycStatusLabel: { type: Function, required: true },
    getRiskLevelLabel: { type: Function, required: true },
    getVerificationModeLabel: { type: Function, required: true },
    guidelineOptions: { type: Array, default: () => [] },
    handleCustomerSelection: { type: Function, required: true },
    isLoading: { type: Boolean, default: false },
    isSubmitting: { type: Boolean, default: false },
    isUpdatingKyc: { type: Boolean, default: false },
    loanQuotes: { type: Array, default: () => [] },
    newCustomer: { type: Object, required: true },
    newCustomerAml: { type: Object, required: true },
    newCustomerKyc: { type: Object, required: true },
    openSelectedCustomerDetails: { type: Function, required: true },
    positions: { type: Array, default: () => [] },
    prefillNewCustomerDocumentData: { type: Function, required: true },
    pledgePresentation: { type: Object, required: true },
    providerVerificationAvailable: { type: Boolean, default: false },
    removePosition: { type: Function, required: true },
    resolveDocumentImageSrc: { type: Function, required: true },
    searchCustomerSuggestions: { type: Function, required: true },
    selectedCustomer: { type: Object, default: null },
    selectedCustomerOption: { type: Object, default: null },
    startProviderVerification: { type: Function, required: true },
    submitLoan: { type: Function, required: true },
    successMessage: { type: String, default: "" },
    t: { type: Function, required: true },
    terms: { type: Object, required: true },
    totalLoanValue: { type: [String, Number], default: "0" },
    updateNewCustomerDocument: { type: Function, required: true },
    updatePowerOfAttorneyDocument: { type: Function, required: true },
    useNewCustomer: { type: Boolean, default: false }
  },
  emits: ["update:selectedCustomerOption", "update:useNewCustomer"],
  setup(props, { emit }) {
    const selectedCustomerOptionModel = computed({
      get: () => props.selectedCustomerOption,
      set: (value) => emit("update:selectedCustomerOption", value)
    });
    const useNewCustomerModel = computed({
      get: () => props.useNewCustomer,
      set: (value) => emit("update:useNewCustomer", value)
    });
    const selectedCustomerAmlDecisionText = computed(
      () => props.selectedCustomer?.amlDecisionReason || props.t("tenantHome.customerSection.amlReviewRequired")
    );

    return {
      selectedCustomerAmlDecisionText,
      selectedCustomerOptionModel,
      useNewCustomerModel
    };
  },
  template
});
