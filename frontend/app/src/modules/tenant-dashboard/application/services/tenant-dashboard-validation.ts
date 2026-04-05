type NewCustomer = {
  customerNumber: string;
  firstName: string;
  lastName: string;
  birthDate: string;
  phone: string;
  email: string;
  wantsDigitalPawnTicket: boolean;
};

type PledgePresentation = {
  thirdPartyPledgorPresentation: boolean;
  bearerName: string;
  powerOfAttorneyDocumentDataUrl: string;
};

type NewCustomerKyc = {
  documentType: string;
  documentNumber: string;
  documentValidUntil: string;
  documentFrontImageDataUrl: string;
  documentBackImageDataUrl: string;
};

type NewCustomerAml = {
  status: string;
  riskLevel: string;
  pepFlag: boolean;
  sanctionsHit: boolean;
  unusualTransactionFlag: boolean;
  sourceOfFundsChecked: boolean;
  suspiciousActivityReported: boolean;
  goamlReference: string;
};

type ExistingCustomerState = {
  id?: string;
  kycApproved?: boolean | null;
  amlOriginationAllowed?: boolean | null;
};

type PositionState = {
  ticketGroup: string | number;
  label: string;
  description: string;
  guidelineId: string;
  pledgedValue: string | number;
};

export function hasText(value: unknown) {
  return String(value ?? "").trim().length > 0;
}

export function hasRequiredNewCustomerFields(
    newCustomer: NewCustomer,
    newCustomerKyc: NewCustomerKyc
) {
  const requiredFields = [
    newCustomer.customerNumber,
    newCustomer.firstName,
    newCustomer.lastName,
    newCustomer.birthDate,
    newCustomer.phone,
    newCustomerKyc.documentType,
    newCustomerKyc.documentNumber,
    newCustomerKyc.documentValidUntil,
    newCustomerKyc.documentFrontImageDataUrl,
    newCustomerKyc.documentBackImageDataUrl
  ];

  return requiredFields.every(hasText);
}

export function hasValidDigitalTicketContact(newCustomer: NewCustomer) {
  return !newCustomer.wantsDigitalPawnTicket || hasText(newCustomer.email);
}

export function hasValidPledgorPresentation(pledgePresentation: PledgePresentation) {
  return !pledgePresentation.thirdPartyPledgorPresentation
    || (hasText(pledgePresentation.bearerName) && hasText(pledgePresentation.powerOfAttorneyDocumentDataUrl));
}

export function hasValidNewCustomerAmlState(
    newCustomerAml: NewCustomerAml,
    amlFeatureEnabled: boolean
) {
  if (!amlFeatureEnabled) {
    return true;
  }

  const suspiciousActivityDocumented =
    !newCustomerAml.suspiciousActivityReported || hasText(newCustomerAml.goamlReference);
  const highRiskPepDocumented =
    !(newCustomerAml.pepFlag && newCustomerAml.riskLevel === "HIGH")
      || newCustomerAml.sourceOfFundsChecked;

  return newCustomerAml.status === "CLEAR"
    && !newCustomerAml.sanctionsHit
    && !newCustomerAml.unusualTransactionFlag
    && suspiciousActivityDocumented
    && highRiskPepDocumented;
}

export function hasValidExistingCustomerState(
    selectedCustomerId: string,
    selectedCustomer: ExistingCustomerState | null,
    amlFeatureEnabled: boolean
) {
  const hasSelectedCustomer = hasText(selectedCustomerId);
  const hasApprovedKyc = selectedCustomer?.kycApproved === true;
  const hasAllowedAml = !amlFeatureEnabled || selectedCustomer?.amlOriginationAllowed === true;

  return hasSelectedCustomer && hasApprovedKyc && hasAllowedAml;
}

export function hasValidPosition(position: PositionState) {
  const hasValidTicketGroup = Number(position.ticketGroup) >= 1 && Number.isInteger(Number(position.ticketGroup));
  const hasRequiredTexts =
    hasText(position.label) && hasText(position.description) && hasText(position.guidelineId);
  const hasPositivePledgedValue = Number(position.pledgedValue) > 0;

  return hasValidTicketGroup && hasRequiredTexts && hasPositivePledgedValue;
}

export function hasValidManualFeeWhenRequired(
    loanQuotes: Array<{ manualMonthlyOperatingFeeRequired?: boolean }>,
    manualMonthlyOperatingFee: string
) {
  const requiresManualFee = loanQuotes.some((quote) => quote.manualMonthlyOperatingFeeRequired);
  return !requiresManualFee || Number(manualMonthlyOperatingFee) >= 0;
}

export function hasRequiredManualKycDocuments(documents: {
  documentFrontImageDataUrl?: string | null;
  documentBackImageDataUrl?: string | null;
}) {
  return Boolean(documents.documentFrontImageDataUrl && documents.documentBackImageDataUrl);
}
