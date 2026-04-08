import type { AmlRiskLevel, AmlStatus, KycStatus, KycVerificationMode } from "../model/customer-enums";

export interface CustomerDto {
  id: string;
  customerNumber: string;
  firstName: string;
  lastName: string;
  birthDate: string;
  displayName: string;
  phone: string;
  email: string;
  wantsDigitalPawnTicket: boolean;
  onlineAccessStatus: string;
  kycStatus: KycStatus;
  kycApproved: boolean;
  kycDocumentType: string | null;
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
    checkedDocumentType: string | null;
    powerOfAttorneyRequired: boolean;
    bearerName: string | null;
  };
  positions: Array<{
    positionNumber: number;
    description: string;
    weightGram: number | null;
    purity: string | null;
    estimatedValue: number;
    loanAmount: number;
  }>;
  pawnTickets: Array<{
    contractNumber: string;
    ticketNumber: string;
    contractBarcode: string;
    createdAt: string;
    dueDate: string;
    earliestAuctionDate: string | null;
    loanAmount: number;
    totalRepaymentAmount: number;
    positionCount: number;
  }>;
}

export interface KycStatusDto {
  customerId: string;
  status: KycStatus;
  verificationMode: KycVerificationMode;
  verifiedUntil: string | null;
  documentType: string | null;
  documentNumber: string | null;
  documentValidUntil: string | null;
  decisionNote: string | null;
  providerName: string | null;
  providerReference: string | null;
  providerStatus: string | null;
  providerVerificationAvailable: boolean;
}

export interface KycDocumentsDto {
  documentFrontImageDataUrl: string | null;
  documentBackImageDataUrl: string | null;
}

export interface AmlStatusDto {
  customerId: string;
  status: AmlStatus;
  riskLevel: AmlRiskLevel;
  pepFlag: boolean;
  sanctionsHit: boolean;
  unusualTransactionFlag: boolean;
  sourceOfFundsChecked: boolean;
  suspiciousActivityReported: boolean;
  goamlReference: string | null;
  decisionNote: string | null;
  lastScreenedAt: string | null;
  reviewedAt: string | null;
  featureAvailable: boolean;
  originationAllowed: boolean;
  decisionReason: string | null;
}


