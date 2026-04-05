import * as amlApi from "../api/aml.api";
import * as customerApi from "../api/customer.api";
import * as kycApi from "../api/kyc.api";
import * as originationApi from "../../../loans/infrastructure/api/origination.api";
import type { AmlStatusDto, CustomerDto, KycDocumentsDto, KycStatusDto, LoanDto } from "../dto/customer-response.dto";

type KycPrefillResult = {
  available?: boolean;
  matched?: boolean;
  documentType?: string | null;
  documentNumber?: string | null;
  documentValidUntil?: string | null;
  portraitImageDataUrl?: string | null;
};

export function createHttpCustomerAdapter() {
  return {
    fetchAmlStatus(tenantId: string, customerId: string): Promise<Record<string, unknown>> {
      return amlApi.fetchAmlStatus(tenantId, customerId) as Promise<Record<string, unknown>>;
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
      ) => Promise<LoanDto[]>)(tenantId, customerId);
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
    prefillKycDocument(tenantId: string, customerId: string, payload: object): Promise<KycPrefillResult> {
      return kycApi.prefillKycDocument(tenantId, customerId, payload) as Promise<KycPrefillResult>;
    },
    searchCustomers(tenantId: string, query: string): Promise<Record<string, unknown>[]> {
      return customerApi.searchCustomers(tenantId, query) as Promise<Record<string, unknown>[]>;
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
