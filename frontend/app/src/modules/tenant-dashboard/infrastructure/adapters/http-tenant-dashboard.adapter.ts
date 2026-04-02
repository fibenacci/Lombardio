import * as amlApi from "../../../customers/infrastructure/api/aml.api";
import * as customerApi from "../../../customers/infrastructure/api/customer.api";
import * as kycApi from "../../../customers/infrastructure/api/kyc.api";
import * as originationApi from "../../../loans/infrastructure/api/origination.api";
import * as pawnTicketApi from "../../../pawn-tickets/infrastructure/api/pawn-ticket.api";
import * as reportingApi from "../api/reporting.api";
import type {
  TenantHomeAmlStatusDto,
  TenantHomeCustomerDto,
  TenantHomeGuidelineDto,
  TenantHomeKycDocumentsPrefillDto,
  TenantHomeKycStatusDto,
  TenantHomeLoanDto,
  TenantHomeQuoteDto,
  TenantHomeReportingOverviewDto
} from "../dto/tenant-dashboard.dto";

export function fetchTenantHomeGuidelines(tenantId: string) {
  return (originationApi.fetchValuationGuidelines as (tenantId: string) => Promise<TenantHomeGuidelineDto[]>)(
    tenantId
  );
}

export function searchTenantHomeCustomers(tenantId: string, query: string) {
  return (customerApi.searchCustomers as (tenantId: string, query: string) => Promise<TenantHomeCustomerDto[]>)(
    tenantId,
    query
  );
}

export function fetchTenantHomeAmlStatus(tenantId: string, customerId: string) {
  return (amlApi.fetchAmlStatus as (tenantId: string, customerId: string) => Promise<TenantHomeAmlStatusDto>)(
    tenantId,
    customerId
  );
}

export function assessTenantHomeAmlOrigination(tenantId: string, customerId: string, payload: { loanAmount: number }) {
  return (amlApi.assessAmlOrigination as (
    tenantId: string,
    customerId: string,
    payload: { loanAmount: number }
  ) => Promise<TenantHomeAmlStatusDto>)(tenantId, customerId, payload);
}

export function fetchTenantHomeKycStatus(tenantId: string, customerId: string) {
  return (kycApi.fetchKycStatus as (tenantId: string, customerId: string) => Promise<TenantHomeKycStatusDto>)(
    tenantId,
    customerId
  );
}

export function updateTenantHomeKycStatus(tenantId: string, customerId: string, payload: Record<string, unknown>) {
  return (kycApi.updateKycStatus as (
    tenantId: string,
    customerId: string,
    payload: Record<string, unknown>
  ) => Promise<TenantHomeKycStatusDto>)(tenantId, customerId, payload);
}

export function prefillTenantHomeKycDocument(tenantId: string, customerId: string, payload: Record<string, unknown>) {
  return (kycApi.prefillKycDocument as (
    tenantId: string,
    customerId: string,
    payload: Record<string, unknown>
  ) => Promise<TenantHomeKycDocumentsPrefillDto>)(tenantId, customerId, payload);
}

export function createTenantHomeCustomer(tenantId: string, payload: Record<string, unknown>) {
  return (customerApi.createCustomer as (tenantId: string, payload: Record<string, unknown>) => Promise<TenantHomeCustomerDto>)(
    tenantId,
    payload
  );
}

export function updateTenantHomeAmlStatus(tenantId: string, customerId: string, payload: Record<string, unknown>) {
  return (amlApi.updateAmlStatus as (
    tenantId: string,
    customerId: string,
    payload: Record<string, unknown>
  ) => Promise<TenantHomeAmlStatusDto>)(tenantId, customerId, payload);
}

export function fetchTenantHomeQuote(payload: Record<string, unknown>) {
  return (pawnTicketApi.fetchPawnTicketQuote as (payload: Record<string, unknown>) => Promise<TenantHomeQuoteDto>)(
    payload
  );
}

export function createTenantHomeLoan(tenantId: string, payload: Record<string, unknown>) {
  return (originationApi.createLoan as (
    tenantId: string,
    payload: Record<string, unknown>
  ) => Promise<TenantHomeLoanDto>)(tenantId, payload);
}

export function fetchTenantHomeReportingOverview(tenantId: string, rangeDays = 14) {
  return (reportingApi.fetchDashboardOverview as (
    tenantId: string,
    rangeDays?: number
  ) => Promise<TenantHomeReportingOverviewDto>)(tenantId, rangeDays);
}

export function fetchTenantHomePawnTicketDocument(ticketNumber: string) {
  return pawnTicketApi.fetchPawnTicketDocument(ticketNumber);
}

export function fetchTenantHomePawnTicketLabels(ticketNumber: string) {
  return pawnTicketApi.fetchPawnTicketLabels(ticketNumber);
}
