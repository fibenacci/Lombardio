import type { 
  IdentityComponents, 
  LoanComponents, 
  ReportingComponents 
} from '../../../../shared/kernel/http/api-types/index';

export type TenantHomeCustomerDto = IdentityComponents['schemas']['CustomerView'];
export type TenantHomeAmlStatusDto = IdentityComponents['schemas']['AmlStatusView'];
export type TenantHomeKycStatusDto = IdentityComponents['schemas']['KycStatusView'];
export type TenantHomeKycDocumentsPrefillDto = IdentityComponents['schemas']['DocumentPrefillView'];

export type TenantHomeGuidelineDto = LoanComponents['schemas']['ValuationGuidelineResponse'];
export type TenantHomeQuoteDto = LoanComponents['schemas']['PawnTicketResponse'];
export type TenantHomeLoanDto = LoanComponents['schemas']['LoanCaseResponse'];

export type TenantHomeReportingOverviewDto = ReportingComponents['schemas']['ReportingDashboardResponse'];
