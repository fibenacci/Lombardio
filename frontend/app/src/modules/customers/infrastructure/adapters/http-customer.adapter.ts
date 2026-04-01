import * as amlApi from "../api/aml.api";
import * as customerApi from "../api/customer.api";
import * as kycApi from "../api/kyc.api";
import * as originationApi from "../../../loans/infrastructure/api/origination.api";
import type { AmlStatusDto, CustomerDto, KycDocumentsDto, KycStatusDto, LoanDto } from "../dto/customer-response.dto";

export function createHttpCustomerAdapter() {
  return {
    fetchAmlStatus(tenantId: string, customerId: string, token: string) {
      return amlApi.fetchAmlStatus(tenantId, customerId, token);
    },
    async loadCustomerDetailData(tenantId: string, customerId: string, token: string, amlEnabled: boolean) {
      const [customerResponse, kycResponse, kycDocumentsResponse] = await Promise.all([
        (customerApi.fetchCustomer as (
          tenantId: string,
          customerId: string,
          token: string
        ) => Promise<CustomerDto>)(tenantId, customerId, token),
        (kycApi.fetchKycStatus as (
          tenantId: string,
          customerId: string,
          token: string
        ) => Promise<KycStatusDto>)(tenantId, customerId, token),
        (kycApi.fetchKycDocuments as (
          tenantId: string,
          customerId: string,
          token: string
        ) => Promise<KycDocumentsDto>)(tenantId, customerId, token)
      ]);
      const loanResponse = await (originationApi.fetchLoans as (
        tenantId: string,
        token: string,
        customerId?: string | null
      ) => Promise<LoanDto[]>)(tenantId, token, customerId);
      const amlResponse = amlEnabled
        ? await (amlApi.fetchAmlStatus as (
          tenantId: string,
          customerId: string,
          token: string
        ) => Promise<AmlStatusDto>)(tenantId, customerId, token)
        : null;

      return {
        aml: amlResponse,
        customer: customerResponse,
        kycDocuments: kycDocumentsResponse,
        kycStatus: kycResponse,
        loans: loanResponse
      };
    },
    prefillKycDocument(tenantId: string, customerId: string, payload: object, token: string) {
      return kycApi.prefillKycDocument(tenantId, customerId, payload, token);
    },
    searchCustomers(tenantId: string, query: string, token: string) {
      return customerApi.searchCustomers(tenantId, query, token);
    },
    saveAml(tenantId: string, customerId: string, payload: object, token: string) {
      return (amlApi.updateAmlStatus as (
        tenantId: string,
        customerId: string,
        payload: object,
        token: string
      ) => Promise<AmlStatusDto>)(tenantId, customerId, payload, token);
    },
    saveCustomer(tenantId: string, customerId: string, payload: object, token: string) {
      return (customerApi.updateCustomer as (
        tenantId: string,
        customerId: string,
        payload: object,
        token: string
      ) => Promise<CustomerDto>)(tenantId, customerId, payload, token);
    },
    saveKyc(tenantId: string, customerId: string, payload: object, token: string) {
      return (kycApi.updateKycStatus as (
        tenantId: string,
        customerId: string,
        payload: object,
        token: string
      ) => Promise<KycStatusDto>)(tenantId, customerId, payload, token);
    }
  };
}
