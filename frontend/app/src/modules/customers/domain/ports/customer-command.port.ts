import type { AmlStatusDto, CustomerDto, KycStatusDto } from "../../infrastructure/dto/customer-response.dto";

export interface CustomerCommandPort {
  prefillKycDocument(tenantId: string, customerId: string, payload: object, token: string): Promise<unknown>;
  saveAml(tenantId: string, customerId: string, payload: object, token: string): Promise<AmlStatusDto>;
  saveCustomer(tenantId: string, customerId: string, payload: object, token: string): Promise<CustomerDto>;
  saveKyc(tenantId: string, customerId: string, payload: object, token: string): Promise<KycStatusDto>;
}
