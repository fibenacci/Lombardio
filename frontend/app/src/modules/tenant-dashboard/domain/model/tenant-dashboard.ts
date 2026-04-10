import type { IdentityComponents } from "../../../../shared/kernel/http/api-types/index";
import { 
  AmlStatusViewStatus as AmlStatus, 
  AmlStatusViewRiskLevel as AmlRiskLevel,
  KycStatusViewStatus as KycStatus,
  KycStatusViewVerificationMode as KycVerificationMode
} from "../../../../shared/kernel/http/api-types/index";

type CustomerView = IdentityComponents["schemas"]["CustomerView"];
type KycStatusView = IdentityComponents["schemas"]["KycStatusView"];
type AmlStatusView = IdentityComponents["schemas"]["AmlStatusView"];

export type TenantHomeCustomerModel = CustomerView & {
  // Use original field names from previous model to avoid breaking UI and tests
  kycStatus: KycStatus;
  verificationMode: KycVerificationMode;
  amlStatus: AmlStatus;
  amlRiskLevel: AmlRiskLevel;
  originationAllowed: AmlStatusView["originationAllowed"];
  
  // Explicitly add other needed fields
  verifiedUntil: KycStatusView["verifiedUntil"];
  documentType: KycStatusView["documentType"];
  documentNumber: KycStatusView["documentNumber"];
  documentValidUntil: KycStatusView["documentValidUntil"];
  
  // Fields needed by UI/Tests that might be in StatusView but naming varies
  providerStatus: string;
  providerName: string;
  providerReference: string;
  
  // Legacy status mapping needed for some components/tests
  kycApproved: boolean;
  
  documentFrontImageDataUrl: string;
  documentBackImageDataUrl: string;
};

// Re-export for module use
export { AmlStatus, AmlRiskLevel, KycStatus, KycVerificationMode };

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
