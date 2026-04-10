import type { 
  CustomerModel, 
  CustomerKycModel, 
  CustomerAmlModel, 
  CustomerLoanModel 
} from "../../domain/model/customer";
import type { components } from "../api/types/identity";

export type KycDocumentsDto = components["schemas"]["KycDocumentImagesView"];

/**
 * Since Backend DTOs are now hardened and mandatory, 
 * we can use them directly as Models in many cases.
 * Transformations are only needed for specific UI formats (like Date strings).
 */

export function mapCustomerDtoToDomain(dto: components["schemas"]["CustomerView"]): CustomerModel {
  return dto;
}

export function mapLoanDtosToDomain(dtos: CustomerLoanModel[]): CustomerLoanModel[] {
  return dtos;
}

export function mapKycToDomain(statusDto: CustomerKycModel, documentsDto: KycDocumentsDto): CustomerKycModel & { documentFrontImageDataUrl?: string | null, documentBackImageDataUrl?: string | null } {
  return {
    ...statusDto,
    documentFrontImageDataUrl: documentsDto.documentFrontImageDataUrl,
    documentBackImageDataUrl: documentsDto.documentBackImageDataUrl
  };
}

export function mapAmlToDomain(dto: CustomerAmlModel): CustomerAmlModel {
  return {
    ...dto,
    lastScreenedAt: toDateTimeLocal(dto.lastScreenedAt),
    reviewedAt: toDateTimeLocal(dto.reviewedAt)
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
    city: model.city,
    kycDocumentType: model.kycDocumentType || null
  };
}

export function mapKycDomainToUpdatePayload(model: CustomerKycModel, images: KycDocumentsDto) {
  return {
    customerId: model.customerId,
    status: model.status,
    verificationMode: model.verificationMode,
    verifiedUntil: model.verifiedUntil,
    documentType: model.documentType,
    documentNumber: model.documentNumber,
    documentValidUntil: model.documentValidUntil,
    documentFrontImageDataUrl: images.documentFrontImageDataUrl,
    documentBackImageDataUrl: images.documentBackImageDataUrl,
    decisionNote: model.decisionNote,
    providerName: model.providerName,
    providerReference: model.providerReference,
    providerStatus: model.providerStatus
  };
}

export function mapAmlDomainToUpdatePayload(model: CustomerAmlModel) {
  return {
    customerId: model.customerId,
    status: model.status,
    riskLevel: model.riskLevel,
    pepFlag: model.pepFlag,
    sanctionsHit: model.sanctionsHit,
    unusualTransactionFlag: model.unusualTransactionFlag,
    sourceOfFundsChecked: model.sourceOfFundsChecked,
    suspiciousActivityReported: model.suspiciousActivityReported,
    goamlReference: model.goamlReference || "",
    decisionNote: model.decisionNote || "",
    lastScreenedAt: toInstant(model.lastScreenedAt),
    reviewedAt: toInstant(model.reviewedAt),
    featureAvailable: model.featureAvailable,
    originationAllowed: model.originationAllowed,
    decisionReason: model.decisionReason || ""
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

function toInstant(value: string | null) {
  if (!value) {
    return null;
  }
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? null : date.toISOString();
}
