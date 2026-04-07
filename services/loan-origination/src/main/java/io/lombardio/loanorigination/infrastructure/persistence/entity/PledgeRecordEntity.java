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
package io.lombardio.loanorigination.infrastructure.persistence.entity;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pledge_records")
@Getter
@Setter
@NoArgsConstructor
@SuppressFBWarnings(
    value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"},
    justification = "JPA entities")
public class PledgeRecordEntity {

  @Id private String id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "loan_case_id", nullable = false)
  private LoanCaseEntity loanCase;

  @Column(name = "tenant_id", nullable = false)
  private String tenantId;

  @Column(name = "recorded_at", nullable = false)
  private Instant recordedAt;

  @Column(name = "language_code", nullable = false)
  private String languageCode;

  @Column(name = "retention_until", nullable = false)
  private LocalDate retentionUntil;

  @Column(name = "pledgor_name", nullable = false)
  private String pledgorName;

  @Column(name = "pledgor_street")
  private String pledgorStreet;

  @Column(name = "pledgor_postal_code")
  private String pledgorPostalCode;

  @Column(name = "pledgor_city")
  private String pledgorCity;

  @Column(name = "pledgor_birth_date")
  private LocalDate pledgorBirthDate;

  @Column(name = "checked_document_type")
  private String checkedDocumentType;

  @Column(name = "power_of_attorney_required", nullable = false)
  private boolean powerOfAttorneyRequired;

  @Column(name = "bearer_name")
  private String bearerName;

  @Column(name = "bearer_street")
  private String bearerStreet;

  @Column(name = "bearer_postal_code")
  private String bearerPostalCode;

  @Column(name = "bearer_city")
  private String bearerCity;

  @Column(name = "power_of_attorney_document_data_url")
  private String powerOfAttorneyDocumentDataUrl;

  @Column(name = "sort_order", nullable = false)
  private Integer sortOrder;
}
