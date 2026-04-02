import type { AmlStatusDto, CustomerDto, KycStatusDto } from "../../infrastructure/dto/customer-response.dto";

export interface CustomerCommandPort {
  prefillKycDocument(tenantId: string, customerId: string, payload: object): Promise<unknown>;
  saveAml(tenantId: string, customerId: string, payload: object): Promise<AmlStatusDto>;
  saveCustomer(tenantId: string, customerId: string, payload: object): Promise<CustomerDto>;
  saveKyc(tenantId: string, customerId: string, payload: object): Promise<KycStatusDto>;
}
