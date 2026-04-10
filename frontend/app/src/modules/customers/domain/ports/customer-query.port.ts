import type { 
  CustomerDto, 
  KycDocumentsDto, 
  KycStatusDto, 
  AmlStatusDto,
  KycPrefillDto 
} from "../../infrastructure/adapters/http-customer.adapter";
import type { LoanModel } from "../../../loans/domain/model/loan";

export interface CustomerQueryPort {
  loadCustomerDetailData(tenantId: string, customerId: string, amlEnabled: boolean): Promise<{
    customer: CustomerDto;
    kycStatus: KycStatusDto;
    kycDocuments: KycDocumentsDto;
    aml: AmlStatusDto | null;
    loans: LoanModel[];
  }>;
  searchCustomers(tenantId: string, query: string): Promise<CustomerDto[]>;
  fetchAmlStatus(tenantId: string, customerId: string): Promise<AmlStatusDto>;
  prefillKycDocument(tenantId: string, customerId: string, payload: object): Promise<KycPrefillDto>;
}
