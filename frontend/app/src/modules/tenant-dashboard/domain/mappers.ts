import type {
  TenantHomeAmlStatusDto,
  TenantHomeCustomerDto,
  TenantHomeKycStatusDto,
  TenantHomeReportingOverviewDto
} from "../infrastructure/dto/tenant-dashboard.dto";
import {
  type TenantHomeAmlDraftModel,
  type TenantHomeCustomerModel,
  type TenantHomeKycDraftModel,
  type TenantHomePositionModel,
  KycStatus,
  KycVerificationMode,
  AmlStatus,
  AmlRiskLevel
} from "./model/tenant-dashboard";

export function createEmptyNewCustomerKyc(): TenantHomeKycDraftModel {
  return {
    documentType: "PERSONALAUSWEIS",
    documentNumber: "",
    documentValidUntil: "",
    documentFrontImageDataUrl: "",
    documentBackImageDataUrl: "",
    portraitImageDataUrl: ""
  };
}

export function createEmptyPosition(): TenantHomePositionModel {
  return {
    ticketGroup: 1,
    label: "",
    description: "",
    guidelineId: "",
    pledgedValue: ""
  };
}

export function createEmptyNewCustomerAml(): TenantHomeAmlDraftModel {
  return {
    status: "CLEAR",
    riskLevel: "MEDIUM",
    pepFlag: false,
    sanctionsHit: false,
    unusualTransactionFlag: false,
    sourceOfFundsChecked: false,
    suspiciousActivityReported: false,
    goamlReference: "",
    decisionNote: ""
  };
}

export function mergeKycStatus(
  customer: TenantHomeCustomerModel,
  kycStatus: TenantHomeKycStatusDto
): TenantHomeCustomerModel {
  return {
    ...customer,
    kycStatus: kycStatus.status,
    verificationMode: kycStatus.verificationMode,
    verifiedUntil: kycStatus.verifiedUntil,
    documentType: kycStatus.documentType,
    documentNumber: kycStatus.documentNumber,
    documentValidUntil: kycStatus.documentValidUntil,
    providerStatus: kycStatus.providerStatus,
    providerName: kycStatus.providerName,
    providerReference: kycStatus.providerReference,
    kycApproved: kycStatus.status === KycStatus.APPROVED
  };
}

export function mergeKycDocuments(
  customer: TenantHomeCustomerModel,
  kycDocuments: { documentFrontImageDataUrl?: string | null; documentBackImageDataUrl?: string | null }
): TenantHomeCustomerModel {
  return {
    ...customer,
    documentFrontImageDataUrl: kycDocuments.documentFrontImageDataUrl ?? customer.documentFrontImageDataUrl ?? "",
    documentBackImageDataUrl: kycDocuments.documentBackImageDataUrl ?? customer.documentBackImageDataUrl ?? ""
  };
}

export function mergeAmlStatus(
  customer: TenantHomeCustomerModel,
  amlStatus: TenantHomeAmlStatusDto
): TenantHomeCustomerModel {
  return {
    ...customer,
    amlStatus: amlStatus.status,
    amlRiskLevel: amlStatus.riskLevel,
    originationAllowed: amlStatus.originationAllowed
  };
}

export function formatCustomerOption(customer: TenantHomeCustomerModel, t: (key: string, params?: Record<string, unknown>) => string) {
  const kycStatus = customer.kycStatus ?? KycStatus.NOT_STARTED;
  return {
    value: customer.id,
    label: t("tenantHome.customerOption", {
      customerNumber: customer.customerNumber,
      displayName: customer.displayName,
      kycStatus: t(`customerDetail.statusOptions.kyc.${kycStatus}`)
    })
  };
}

export function matchesCustomerQuery(customer: TenantHomeCustomerModel, query: string) {
  const normalizedQuery = query.trim().toLowerCase();

  if (!normalizedQuery) {
    return true;
  }

  return [
    customer.customerNumber,
    customer.displayName,
    customer.firstName,
    customer.lastName,
    customer.phone
  ]
    .some((value) => String(value).toLowerCase().includes(normalizedQuery));
}

export function isRecoverableStartupError(error: unknown) {
  return [502, 503, 504].includes(Number((error as { status?: unknown } | undefined)?.status));
}

export function createDocumentTypeOptions(t: (key: string) => string) {
  return [
    { value: "PERSONALAUSWEIS", label: t("customerDetail.documentTypeOptions.PERSONALAUSWEIS") },
    { value: "REISEPASS", label: t("customerDetail.documentTypeOptions.REISEPASS") },
    { value: "AUFENTHALTSTITEL", label: t("customerDetail.documentTypeOptions.AUFENTHALTSTITEL") }
  ];
}

export function createAmlStatusOptions(t: (key: string) => string) {
  return [
    { value: AmlStatus.NOT_REVIEWED, label: t("customerDetail.statusOptions.aml.NOT_REVIEWED") },
    { value: AmlStatus.CLEAR, label: t("customerDetail.statusOptions.aml.CLEAR") },
    { value: AmlStatus.REVIEW_REQUIRED, label: t("customerDetail.statusOptions.aml.REVIEW_REQUIRED") },
    { value: AmlStatus.BLOCKED, label: t("customerDetail.statusOptions.aml.BLOCKED") },
    { value: AmlStatus.REPORTED, label: t("customerDetail.statusOptions.aml.REPORTED") }
  ];
}

export function createAmlRiskLevelOptions(t: (key: string) => string) {
  return [
    { value: AmlRiskLevel.LOW, label: t("customerDetail.riskLevels.LOW") },
    { value: AmlRiskLevel.MEDIUM, label: t("customerDetail.riskLevels.MEDIUM") },
    { value: AmlRiskLevel.HIGH, label: t("customerDetail.riskLevels.HIGH") }
  ];
}

export function getVerificationModeLabel(t: (key: string) => string, mode: string | null | undefined) {
  const labels = {
    MANUAL: t("tenantHome.verificationModes.MANUAL"),
    PROVIDER: t("tenantHome.verificationModes.PROVIDER")
  };

  return labels[mode as keyof typeof labels] ?? mode ?? t("common.notAvailable");
}

export function getTransactionTypeLabel(t: (key: string) => string, type: string | null | undefined) {
  const key = `tenantHome.transactionTypes.${type}`;
  const translated = t(key);
  return translated === key ? (type ?? t("common.notAvailable")) : translated;
}

export function getKycStatusLabel(t: (key: string) => string, status: string | null | undefined) {
  return t(`customerDetail.statusOptions.kyc.${status ?? KycStatus.NOT_STARTED}`);
}

export function getAmlStatusLabel(t: (key: string) => string, status: string | null | undefined) {
  return t(`customerDetail.statusOptions.aml.${status ?? AmlStatus.NOT_REVIEWED}`);
}

export function getRiskLevelLabel(t: (key: string) => string, level: string | null | undefined) {
  return t(`customerDetail.riskLevels.${level ?? AmlRiskLevel.MEDIUM}`);
}

export function calculateFinanceTrendMax(reportingOverview: TenantHomeReportingOverviewDto | null) {
  if (!reportingOverview) return 1;
  const values = reportingOverview.financeTrend.flatMap((point) => [
    point.cashInflow,
    point.cashOutflow,
    point.realizedRevenue
  ]);

  return Math.max(1, ...values);
}

export function calculateInventoryMax(reportingOverview: TenantHomeReportingOverviewDto | null) {
  if (!reportingOverview) return 1;
  const values = reportingOverview.inventoryByCategory.map((category) => category.pledgedValue);
  return Math.max(1, ...values);
}

export function toCustomerModel(customer: TenantHomeCustomerDto): TenantHomeCustomerModel {
  return {
    ...customer,
    kycStatus: KycStatus.NOT_STARTED,
    verificationMode: KycVerificationMode.MANUAL,
    amlStatus: AmlStatus.NOT_REVIEWED,
    amlRiskLevel: AmlRiskLevel.MEDIUM,
    originationAllowed: false,
    verifiedUntil: "",
    documentType: "",
    documentNumber: "",
    documentValidUntil: "",
    providerStatus: "",
    providerName: "",
    providerReference: "",
    kycApproved: false,
    documentFrontImageDataUrl: "",
    documentBackImageDataUrl: ""
  };
}

export function hasRequiredManualKycDocuments(kyc: {
  documentFrontImageDataUrl?: string | null;
  documentBackImageDataUrl?: string | null;
}): boolean {
  return !!kyc.documentFrontImageDataUrl && !!kyc.documentBackImageDataUrl;
}
