import type { CustomerDto, KycDocumentsDto, KycStatusDto, LoanDto, AmlStatusDto } from "../../infrastructure/dto/customer-response.dto";

export interface CustomerQueryPort {
  loadCustomerDetailData(
    tenantId: string,
    customerId: string,
    token: string,
    amlEnabled: boolean
  ): Promise<{
    aml: AmlStatusDto | null;
    customer: CustomerDto;
    kycDocuments: KycDocumentsDto;
    kycStatus: KycStatusDto;
    loans: LoanDto[];
  }>;
}
