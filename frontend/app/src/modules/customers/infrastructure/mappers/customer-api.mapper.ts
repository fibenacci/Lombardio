import type { AmlStatusDto, CustomerDto, KycDocumentsDto, KycStatusDto, LoanDto } from "../dto/customer-response.dto";
import type { CustomerAmlModel, CustomerKycModel, CustomerLoanModel, CustomerModel } from "../../domain/model/customer";

export function mapCustomerDtoToDomain(dto: CustomerDto): CustomerModel {
  return { ...dto };
}

export function mapLoanDtosToDomain(dtos: LoanDto[]): CustomerLoanModel[] {
  return dtos.map((dto) => ({ ...dto }));
}

export function mapKycToDomain(statusDto: KycStatusDto, documentsDto: KycDocumentsDto): CustomerKycModel {
  return {
    status: statusDto.status,
    verificationMode: statusDto.verificationMode ?? "MANUAL",
    verifiedUntil: statusDto.verifiedUntil ?? "",
    documentType: statusDto.documentType ?? "PERSONALAUSWEIS",
    documentNumber: statusDto.documentNumber ?? "",
    documentValidUntil: statusDto.documentValidUntil ?? "",
    documentFrontImageDataUrl: documentsDto.documentFrontImageDataUrl ?? "",
    documentBackImageDataUrl: documentsDto.documentBackImageDataUrl ?? "",
    portraitImageDataUrl: "",
    decisionNote: statusDto.decisionNote ?? ""
  };
}

export function mapAmlToDomain(dto: AmlStatusDto | null | undefined, featureEnabled: boolean): CustomerAmlModel {
  return {
    status: dto?.status ?? "NOT_REVIEWED",
    riskLevel: dto?.riskLevel ?? "MEDIUM",
    pepFlag: dto?.pepFlag ?? false,
    sanctionsHit: dto?.sanctionsHit ?? false,
    unusualTransactionFlag: dto?.unusualTransactionFlag ?? false,
    sourceOfFundsChecked: dto?.sourceOfFundsChecked ?? false,
    suspiciousActivityReported: dto?.suspiciousActivityReported ?? false,
    goamlReference: dto?.goamlReference ?? "",
    decisionNote: dto?.decisionNote ?? "",
    lastScreenedAt: toDateTimeLocal(dto?.lastScreenedAt),
    reviewedAt: toDateTimeLocal(dto?.reviewedAt),
    originationAllowed: dto?.originationAllowed ?? false,
    decisionReason: dto?.decisionReason ?? "",
    featureAvailable: dto?.featureAvailable ?? featureEnabled
  };
}

export function mapCustomerDomainToUpdatePayload(model: CustomerModel) {
  return {
    customerNumber: model.customerNumber,
    firstName: model.firstName,
    lastName: model.lastName,
    birthDate: model.birthDate,
    phone: model.phone,
    email: model.email,
    wantsDigitalPawnTicket: model.wantsDigitalPawnTicket,
    street: model.street,
    postalCode: model.postalCode,
    city: model.city
  };
}

export function mapKycDomainToUpdatePayload(model: CustomerKycModel) {
  return {
    status: model.status,
    verificationMode: "MANUAL",
    verifiedUntil: model.verifiedUntil || model.documentValidUntil,
    documentType: model.documentType,
    documentNumber: model.documentNumber,
    documentValidUntil: model.documentValidUntil,
    documentFrontImageDataUrl: model.documentFrontImageDataUrl,
    documentBackImageDataUrl: model.documentBackImageDataUrl,
    decisionNote: model.decisionNote,
    providerName: null,
    providerReference: null,
    providerStatus: null
  };
}

export function mapAmlDomainToUpdatePayload(model: CustomerAmlModel) {
  return {
    status: model.status,
    riskLevel: model.riskLevel,
    pepFlag: model.pepFlag,
    sanctionsHit: model.sanctionsHit,
    unusualTransactionFlag: model.unusualTransactionFlag,
    sourceOfFundsChecked: model.sourceOfFundsChecked,
    suspiciousActivityReported: model.suspiciousActivityReported,
    goamlReference: model.goamlReference || null,
    decisionNote: model.decisionNote || null,
    lastScreenedAt: toInstant(model.lastScreenedAt),
    reviewedAt: toInstant(model.reviewedAt)
  };
}

function toDateTimeLocal(value?: string | null) {
  if (!value) {
    return "";
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return "";
  }
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  const hours = String(date.getHours()).padStart(2, "0");
  const minutes = String(date.getMinutes()).padStart(2, "0");
  return `${year}-${month}-${day}T${hours}:${minutes}`;
}

function toInstant(value: string) {
  if (!value) {
    return null;
  }
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? null : date.toISOString();
}
