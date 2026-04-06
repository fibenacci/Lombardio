export interface CustomerDto {
  id: string;
  customerNumber: string;
  firstName: string;
  lastName: string;
  birthDate: string;
  phone: string;
  email: string;
  wantsDigitalPawnTicket: boolean;
  onlineAccessStatus: string;
  street: string;
  postalCode: string;
  city: string;
}

export interface LoanDto {
  id: string;
  pledgeRecord: {
    recordedAt: string;
    languageCode: string;
    retentionUntil: string;
    checkedDocumentType?: string | null;
    powerOfAttorneyRequired: boolean;
    bearerName?: string | null;
  };
  positions: Array<unknown>;
  pawnTickets: Array<{
    ticketNumber: string;
    totalLoanValue: number;
    dueDate: string;
  }>;
}

export interface KycStatusDto {
  status: string;
  verificationMode?: string | null;
  verifiedUntil?: string | null;
  documentType?: string | null;
  documentNumber?: string | null;
  documentValidUntil?: string | null;
  decisionNote?: string | null;
}

export interface KycDocumentsDto {
  documentFrontImageDataUrl?: string | null;
  documentBackImageDataUrl?: string | null;
}

export interface AmlStatusDto {
  status?: string | null;
  riskLevel?: string | null;
  pepFlag?: boolean | null;
  sanctionsHit?: boolean | null;
  unusualTransactionFlag?: boolean | null;
  sourceOfFundsChecked?: boolean | null;
  suspiciousActivityReported?: boolean | null;
  goamlReference?: string | null;
  decisionNote?: string | null;
  lastScreenedAt?: string | null;
  reviewedAt?: string | null;
  featureAvailable?: boolean | null;
  originationAllowed?: boolean | null;
  decisionReason?: string | null;
}
