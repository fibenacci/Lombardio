export interface CustomerModel {
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

export interface CustomerLoanModel {
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

export interface CustomerKycModel {
  status: string;
  verificationMode: string;
  verifiedUntil: string;
  documentType: string;
  documentNumber: string;
  documentValidUntil: string;
  documentFrontImageDataUrl: string;
  documentBackImageDataUrl: string;
  portraitImageDataUrl: string;
  decisionNote: string;
}

export interface CustomerAmlModel {
  status: string;
  riskLevel: string;
  pepFlag: boolean;
  sanctionsHit: boolean;
  unusualTransactionFlag: boolean;
  sourceOfFundsChecked: boolean;
  suspiciousActivityReported: boolean;
  goamlReference: string;
  decisionNote: string;
  lastScreenedAt: string;
  reviewedAt: string;
  originationAllowed: boolean;
  decisionReason: string;
  featureAvailable: boolean;
}
