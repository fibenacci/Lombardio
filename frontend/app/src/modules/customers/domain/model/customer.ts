import type { components } from "../../infrastructure/api/types/identity";
import { 
  AmlStatusViewStatus as AmlStatus, 
  AmlStatusViewRiskLevel as AmlRiskLevel,
  KycStatusViewStatus as KycStatus,
  KycStatusViewVerificationMode as KycVerificationMode
} from "../../infrastructure/api/types/identity";

export type CustomerModel = components["schemas"]["CustomerView"];
export type CustomerKycModel = components["schemas"]["KycStatusView"];
export type CustomerAmlModel = components["schemas"]["AmlStatusView"];

// Re-export generated Enums with cleaner names
export { AmlStatus, AmlRiskLevel, KycStatus, KycVerificationMode };

// Loan model remains manual for now as it's a composite of multiple services/views
export interface CustomerLoanModel {
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
