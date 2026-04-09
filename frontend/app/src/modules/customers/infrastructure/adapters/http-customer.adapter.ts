import * as amlApi from "../api/aml.api";
import * as customerApi from "../api/customer.api";
import * as kycApi from "../api/kyc.api";
import * as originationApi from "../../../loans/infrastructure/api/origination.api";
import type { components } from "../api/types/identity";

export type CustomerDto = components["schemas"]["CustomerView"];
export type KycStatusDto = components["schemas"]["KycStatusView"];
export type KycDocumentsDto = components["schemas"]["KycDocumentImagesView"];
export type AmlStatusDto = components["schemas"]["AmlStatusView"];
export type KycPrefillDto = components["schemas"]["DocumentPrefillView"];

export function createHttpCustomerAdapter() {
  return {
    fetchAmlStatus(tenantId: string, customerId: string): Promise<AmlStatusDto> {
      return amlApi.fetchAmlStatus(tenantId, customerId) as Promise<AmlStatusDto>;
    },
    async loadCustomerDetailData(tenantId: string, customerId: string, amlEnabled: boolean) {
      const [customerResponse, kycResponse, kycDocumentsResponse] = await Promise.all([
        (customerApi.fetchCustomer as (tenantId: string, customerId: string) => Promise<CustomerDto>)(
          tenantId,
          customerId
        ),
        (kycApi.fetchKycStatus as (tenantId: string, customerId: string) => Promise<KycStatusDto>)(
          tenantId,
          customerId
        ),
        (kycApi.fetchKycDocuments as (tenantId: string, customerId: string) => Promise<KycDocumentsDto>)(
          tenantId,
          customerId
        )
      ]);
      const loanResponse = await (originationApi.fetchLoans as (
        tenantId: string,
        customerId?: string | null
      ) => Promise<any[]>)(tenantId, customerId);
      const amlResponse = amlEnabled
        ? await (amlApi.fetchAmlStatus as (tenantId: string, customerId: string) => Promise<AmlStatusDto>)(
          tenantId,
          customerId
        )
        : null;

      return {
        aml: amlResponse,
        customer: customerResponse,
        kycDocuments: kycDocumentsResponse,
        kycStatus: kycResponse,
        loans: loanResponse
      };
    },
    prefillKycDocument(tenantId: string, customerId: string, payload: object): Promise<KycPrefillDto> {
      return kycApi.prefillKycDocument(tenantId, customerId, payload) as Promise<KycPrefillDto>;
    },
    searchCustomers(tenantId: string, query: string): Promise<CustomerDto[]> {
      return customerApi.searchCustomers(tenantId, query) as Promise<CustomerDto[]>;
    },
    saveAml(tenantId: string, customerId: string, payload: object) {
      return (amlApi.updateAmlStatus as (
        tenantId: string,
        customerId: string,
        payload: object
      ) => Promise<AmlStatusDto>)(tenantId, customerId, payload);
    },
    saveCustomer(tenantId: string, customerId: string, payload: object) {
      return (customerApi.updateCustomer as (
        tenantId: string,
        customerId: string,
        payload: object
      ) => Promise<CustomerDto>)(tenantId, customerId, payload);
    },
    saveKyc(tenantId: string, customerId: string, payload: object) {
      return (kycApi.updateKycStatus as (
        tenantId: string,
        customerId: string,
        payload: object
      ) => Promise<KycStatusDto>)(tenantId, customerId, payload);
    }
  };
}
