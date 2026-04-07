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
package io.lombardio.identity.aml.infrastructure.persistence.entity;

import io.lombardio.identity.aml.domain.model.AmlRiskLevel;
import io.lombardio.identity.aml.domain.model.AmlStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "aml_cases")
@Getter
@Setter
@NoArgsConstructor
public class AmlCaseEntity {

  @Id private String id;

  @Column(name = "tenant_id", nullable = false)
  private String tenantId;

  @Column(name = "customer_id", nullable = false)
  private String customerId;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private AmlStatus status;

  @Enumerated(EnumType.STRING)
  @Column(name = "risk_level", nullable = false)
  private AmlRiskLevel riskLevel;

  @Column(name = "pep_flag", nullable = false)
  private boolean pepFlag;

  @Column(name = "sanctions_hit", nullable = false)
  private boolean sanctionsHit;

  @Column(name = "unusual_transaction_flag", nullable = false)
  private boolean unusualTransactionFlag;

  @Column(name = "source_of_funds_checked", nullable = false)
  private boolean sourceOfFundsChecked;

  @Column(name = "suspicious_activity_reported", nullable = false)
  private boolean suspiciousActivityReported;

  @Column(name = "goaml_reference")
  private String goamlReference;

  @Column(name = "decision_note", length = 1000)
  private String decisionNote;

  @Column(name = "last_screened_at")
  private Instant lastScreenedAt;

  @Column(name = "reviewed_at")
  private Instant reviewedAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;
}
