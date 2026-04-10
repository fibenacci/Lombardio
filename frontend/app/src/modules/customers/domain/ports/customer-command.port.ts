import type { AmlStatusDto, CustomerDto, KycStatusDto } from "../../infrastructure/adapters/http-customer.adapter";

export interface CustomerCommandPort {
  saveCustomer(tenantId: string, customerId: string, payload: object): Promise<CustomerDto>;
  saveKyc(tenantId: string, customerId: string, payload: object): Promise<KycStatusDto>;
  saveAml(tenantId: string, customerId: string, payload: object): Promise<AmlStatusDto>;
}
