/*
 * Lombardio Source-Available No-Distribution License 1.0
 *
 * Copyright (c) 2026 Benjamin Letzel. All rights reserved.
 *
 * This project is source-available for educational and review purposes only.
 * Redistribution, sublicensing, or commercial use is strictly prohibited.
 *
 * For partnership or cooperation inquiries, please contact the author.
 */
package io.lombardio.identity.kyc.infrastructure.persistence;

import io.lombardio.identity.kyc.domain.KycStatus;
import io.lombardio.identity.kyc.domain.KycVerificationMode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "kyc_records")
@Getter
@Setter
@NoArgsConstructor
public class KycRecordEntity {

  @Id private String id;

  @Column(name = "tenant_id", nullable = false)
  private String tenantId;

  @Column(name = "customer_id", nullable = false)
  private String customerId;

  @Enumerated(EnumType.STRING)
  @Column(name = "verification_mode", nullable = false)
  private KycVerificationMode verificationMode;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private KycStatus status;

  @Column(name = "verified_until")
  private LocalDate verifiedUntil;

  @Column(name = "document_type")
  private String documentType;

  @Column(name = "document_number")
  private String documentNumber;

  @Column(name = "document_valid_until")
  private LocalDate documentValidUntil;

  @Column(name = "document_front_image_data_url", columnDefinition = "text")
  private String documentFrontImageDataUrl;

  @Column(name = "document_back_image_data_url", columnDefinition = "text")
  private String documentBackImageDataUrl;

  @Column(name = "decision_note")
  private String decisionNote;

  @Column(name = "provider_name")
  private String providerName;

  @Column(name = "provider_reference")
  private String providerReference;

  @Column(name = "provider_status")
  private String providerStatus;
}
