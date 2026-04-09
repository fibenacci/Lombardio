import type { 
  CustomerDto, 
  KycDocumentsDto, 
  KycStatusDto, 
  AmlStatusDto,
  KycPrefillDto 
} from "../../infrastructure/adapters/http-customer.adapter";

export interface CustomerQueryPort {
  loadCustomerDetailData(tenantId: string, customerId: string, amlEnabled: boolean): Promise<{
    customer: CustomerDto;
    kycStatus: KycStatusDto;
    kycDocuments: KycDocumentsDto;
    aml: AmlStatusDto | null;
    loans: any[];
  }>;
  searchCustomers(tenantId: string, query: string): Promise<CustomerDto[]>;
  fetchAmlStatus(tenantId: string, customerId: string): Promise<AmlStatusDto>;
  prefillKycDocument(tenantId: string, customerId: string, payload: object): Promise<KycPrefillDto>;
}
