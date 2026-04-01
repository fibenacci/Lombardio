export interface TenantHomeCustomerModel {
  id: string;
  customerNumber?: string | null;
  displayName?: string | null;
  firstName?: string | null;
  lastName?: string | null;
  birthDate?: string | null;
  phone?: string | null;
  email?: string | null;
  wantsDigitalPawnTicket?: boolean | null;
  street?: string | null;
  postalCode?: string | null;
  city?: string | null;
  kycStatus?: string | null;
  kycApproved?: boolean | null;
  verificationMode?: string | null;
  verifiedUntil?: string | null;
  documentType?: string | null;
  documentNumber?: string | null;
  documentValidUntil?: string | null;
  documentFrontImageDataUrl?: string;
  documentBackImageDataUrl?: string;
  decisionNote?: string | null;
  providerName?: string | null;
  providerReference?: string | null;
  providerStatus?: string | null;
  amlStatus?: string | null;
  amlRiskLevel?: string | null;
  amlOriginationAllowed?: boolean | null;
  amlDecisionReason?: string | null;
  sourceOfFundsChecked?: boolean | null;
  suspiciousActivityReported?: boolean | null;
  goamlReference?: string | null;
}

export interface TenantHomePositionModel {
  ticketGroup: number;
  label: string;
  description: string;
  guidelineId: string;
  pledgedValue: string | number;
}

export interface TenantHomeKycDraftModel {
  documentType: string;
  documentNumber: string;
  documentValidUntil: string;
  documentFrontImageDataUrl: string;
  documentBackImageDataUrl: string;
  portraitImageDataUrl: string;
}

export interface TenantHomeAmlDraftModel {
  status: string;
  riskLevel: string;
  pepFlag: boolean;
  sanctionsHit: boolean;
  unusualTransactionFlag: boolean;
  sourceOfFundsChecked: boolean;
  suspiciousActivityReported: boolean;
  goamlReference: string;
  decisionNote: string;
}
