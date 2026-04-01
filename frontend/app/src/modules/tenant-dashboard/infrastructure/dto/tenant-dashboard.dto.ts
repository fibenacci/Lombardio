export interface TenantHomeCustomerDto {
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
  checkedDocumentType?: string | null;
}

export interface TenantHomeGuidelineDto {
  id: string;
  label: string;
  description: string;
  baseLoanValue: number;
}

export interface TenantHomeQuoteDto {
  contractNumber?: string;
  contractBarcode?: string;
  termsVersion?: string;
  termsAndConditionsText?: string;
  dueDate?: string;
  earliestAuctionDate?: string;
  monthlyInterestRate?: number;
  monthlyOperatingFee?: number;
  manualMonthlyOperatingFeeRequired?: boolean;
  totalInterestAmount?: number;
  totalOperatingFeeAmount?: number;
  totalRepaymentAmount?: number;
  legalText?: string;
  positions?: Array<unknown>;
}

export interface TenantHomeAmlStatusDto {
  status?: string | null;
  riskLevel?: string | null;
  pepFlag?: boolean | null;
  sanctionsHit?: boolean | null;
  unusualTransactionFlag?: boolean | null;
  sourceOfFundsChecked?: boolean | null;
  suspiciousActivityReported?: boolean | null;
  goamlReference?: string | null;
  decisionNote?: string | null;
  featureAvailable?: boolean | null;
  originationAllowed?: boolean | null;
  decisionReason?: string | null;
}

export interface TenantHomeKycStatusDto {
  status?: string | null;
  verificationMode?: string | null;
  verifiedUntil?: string | null;
  documentType?: string | null;
  documentNumber?: string | null;
  documentValidUntil?: string | null;
  decisionNote?: string | null;
  providerName?: string | null;
  providerReference?: string | null;
  providerStatus?: string | null;
}

export interface TenantHomeKycDocumentsPrefillDto {
  available?: boolean;
  matched?: boolean;
  documentType?: string | null;
  documentNumber?: string | null;
  documentValidUntil?: string | null;
  portraitImageDataUrl?: string | null;
  firstName?: string | null;
  lastName?: string | null;
  birthDate?: string | null;
}

export interface TenantHomeReportingOverviewDto {
  financeTrend?: Array<{
    cashInflow?: number | null;
    cashOutflow?: number | null;
    realizedRevenue?: number | null;
  }>;
  inventoryByCategory?: Array<{
    pledgedValue?: number | null;
  }>;
}

export interface TenantHomeLoanDto {
  id?: string;
  pawnTickets?: Array<{
    ticketNumber?: string;
  }>;
}
