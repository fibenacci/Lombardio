export enum AmlStatus {
  NOT_REVIEWED = "NOT_REVIEWED",
  CLEAR = "CLEAR",
  REVIEW_REQUIRED = "REVIEW_REQUIRED",
  BLOCKED = "BLOCKED",
  REPORTED = "REPORTED"
}

export enum AmlRiskLevel {
  LOW = "LOW",
  MEDIUM = "MEDIUM",
  HIGH = "HIGH"
}

export enum KycStatus {
  NOT_STARTED = "NOT_STARTED",
  IN_PROGRESS = "IN_PROGRESS",
  APPROVED = "APPROVED",
  REJECTED = "REJECTED"
}

export enum KycVerificationMode {
  MANUAL = "MANUAL",
  PROVIDER = "PROVIDER"
}
