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

export function fetchTenantHomeGuidelines(tenantId: string, token: string) {
  return (originationApi.fetchValuationGuidelines as (
    tenantId: string,
    token: string
  ) => Promise<TenantHomeGuidelineDto[]>)(tenantId, token);
}

export function searchTenantHomeCustomers(tenantId: string, query: string, token: string) {
  return (customerApi.searchCustomers as (
    tenantId: string,
    query: string,
    token: string
  ) => Promise<TenantHomeCustomerDto[]>)(tenantId, query, token);
}

export function fetchTenantHomeAmlStatus(tenantId: string, customerId: string, token: string) {
  return (amlApi.fetchAmlStatus as (
    tenantId: string,
    customerId: string,
    token: string
  ) => Promise<TenantHomeAmlStatusDto>)(tenantId, customerId, token);
}

export function assessTenantHomeAmlOrigination(tenantId: string, customerId: string, payload: { loanAmount: number }, token: string) {
  return (amlApi.assessAmlOrigination as (
    tenantId: string,
    customerId: string,
    payload: { loanAmount: number },
    token: string
  ) => Promise<TenantHomeAmlStatusDto>)(tenantId, customerId, payload, token);
}

export function fetchTenantHomeKycStatus(tenantId: string, customerId: string, token: string) {
  return (kycApi.fetchKycStatus as (
    tenantId: string,
    customerId: string,
    token: string
  ) => Promise<TenantHomeKycStatusDto>)(tenantId, customerId, token);
}

export function updateTenantHomeKycStatus(tenantId: string, customerId: string, payload: Record<string, unknown>, token: string) {
  return (kycApi.updateKycStatus as (
    tenantId: string,
    customerId: string,
    payload: Record<string, unknown>,
    token: string
  ) => Promise<TenantHomeKycStatusDto>)(tenantId, customerId, payload, token);
}

export function prefillTenantHomeKycDocument(tenantId: string, customerId: string, payload: Record<string, unknown>, token: string) {
  return (kycApi.prefillKycDocument as (
    tenantId: string,
    customerId: string,
    payload: Record<string, unknown>,
    token: string
  ) => Promise<TenantHomeKycDocumentsPrefillDto>)(tenantId, customerId, payload, token);
}

export function createTenantHomeCustomer(tenantId: string, payload: Record<string, unknown>, token: string) {
  return (customerApi.createCustomer as (
    tenantId: string,
    payload: Record<string, unknown>,
    token: string
  ) => Promise<TenantHomeCustomerDto>)(tenantId, payload, token);
}

export function updateTenantHomeAmlStatus(tenantId: string, customerId: string, payload: Record<string, unknown>, token: string) {
  return (amlApi.updateAmlStatus as (
    tenantId: string,
    customerId: string,
    payload: Record<string, unknown>,
    token: string
  ) => Promise<TenantHomeAmlStatusDto>)(tenantId, customerId, payload, token);
}

export function fetchTenantHomeQuote(payload: Record<string, unknown>, token: string) {
  return (pawnTicketApi.fetchPawnTicketQuote as (
    payload: Record<string, unknown>,
    token: string
  ) => Promise<TenantHomeQuoteDto>)(payload, token);
}

export function createTenantHomeLoan(tenantId: string, payload: Record<string, unknown>, token: string) {
  return (originationApi.createLoan as (
    tenantId: string,
    payload: Record<string, unknown>,
    token: string
  ) => Promise<TenantHomeLoanDto>)(tenantId, payload, token);
}

export function fetchTenantHomeReportingOverview(tenantId: string, token: string, rangeDays = 14) {
  return (reportingApi.fetchDashboardOverview as (
    tenantId: string,
    token: string,
    rangeDays?: number
  ) => Promise<TenantHomeReportingOverviewDto>)(tenantId, token, rangeDays);
}

export function fetchTenantHomePawnTicketDocument(ticketNumber: string, token: string) {
  return pawnTicketApi.fetchPawnTicketDocument(ticketNumber, token);
}

export function fetchTenantHomePawnTicketLabels(ticketNumber: string, token: string) {
  return pawnTicketApi.fetchPawnTicketLabels(ticketNumber, token);
}
