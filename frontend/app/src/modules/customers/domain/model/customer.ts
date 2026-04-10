import type { components } from "../../infrastructure/api/types/identity";
import { 
  AmlStatusViewStatus as AmlStatus, 
  AmlStatusViewRiskLevel as AmlRiskLevel,
  KycStatusViewStatus as KycStatus,
  KycStatusViewVerificationMode as KycVerificationMode
} from "../../infrastructure/api/types/identity";
import type { LoanModel as CustomerLoanModel } from "../../../loans/domain/model/loan";

export type CustomerModel = components["schemas"]["CustomerView"];
export type CustomerKycModel = components["schemas"]["KycStatusView"];
export type CustomerAmlModel = components["schemas"]["AmlStatusView"];

// Re-export generated Enums with cleaner names
export { AmlStatus, AmlRiskLevel, KycStatus, KycVerificationMode };

export type { CustomerLoanModel };
