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

@Entity
@Table(name = "aml_cases")
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

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  public String getCustomerId() {
    return customerId;
  }

  public void setCustomerId(String customerId) {
    this.customerId = customerId;
  }

  public AmlStatus getStatus() {
    return status;
  }

  public void setStatus(AmlStatus status) {
    this.status = status;
  }

  public AmlRiskLevel getRiskLevel() {
    return riskLevel;
  }

  public void setRiskLevel(AmlRiskLevel riskLevel) {
    this.riskLevel = riskLevel;
  }

  public boolean isPepFlag() {
    return pepFlag;
  }

  public void setPepFlag(boolean pepFlag) {
    this.pepFlag = pepFlag;
  }

  public boolean isSanctionsHit() {
    return sanctionsHit;
  }

  public void setSanctionsHit(boolean sanctionsHit) {
    this.sanctionsHit = sanctionsHit;
  }

  public boolean isUnusualTransactionFlag() {
    return unusualTransactionFlag;
  }

  public void setUnusualTransactionFlag(boolean unusualTransactionFlag) {
    this.unusualTransactionFlag = unusualTransactionFlag;
  }

  public boolean isSourceOfFundsChecked() {
    return sourceOfFundsChecked;
  }

  public void setSourceOfFundsChecked(boolean sourceOfFundsChecked) {
    this.sourceOfFundsChecked = sourceOfFundsChecked;
  }

  public boolean isSuspiciousActivityReported() {
    return suspiciousActivityReported;
  }

  public void setSuspiciousActivityReported(boolean suspiciousActivityReported) {
    this.suspiciousActivityReported = suspiciousActivityReported;
  }

  public String getGoamlReference() {
    return goamlReference;
  }

  public void setGoamlReference(String goamlReference) {
    this.goamlReference = goamlReference;
  }

  public String getDecisionNote() {
    return decisionNote;
  }

  public void setDecisionNote(String decisionNote) {
    this.decisionNote = decisionNote;
  }

  public Instant getLastScreenedAt() {
    return lastScreenedAt;
  }

  public void setLastScreenedAt(Instant lastScreenedAt) {
    this.lastScreenedAt = lastScreenedAt;
  }

  public Instant getReviewedAt() {
    return reviewedAt;
  }

  public void setReviewedAt(Instant reviewedAt) {
    this.reviewedAt = reviewedAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }
}
