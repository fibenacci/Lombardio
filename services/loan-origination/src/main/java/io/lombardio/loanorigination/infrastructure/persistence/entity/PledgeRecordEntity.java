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
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "pledge_records")
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

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @SuppressFBWarnings(
      value = "EI_EXPOSE_REP",
      justification = "JPA relationship references must expose the managed entity association")
  public LoanCaseEntity getLoanCase() {
    return loanCase;
  }

  @SuppressFBWarnings(
      value = "EI_EXPOSE_REP2",
      justification =
          "JPA relationship references must store the managed entity association directly")
  public void setLoanCase(@NotNull LoanCaseEntity loanCase) {
    this.loanCase = Objects.requireNonNull(loanCase, "loanCase");
  }

  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(@NotNull String tenantId) {
    this.tenantId = Objects.requireNonNull(tenantId, "tenantId");
  }

  public Instant getRecordedAt() {
    return recordedAt;
  }

  public void setRecordedAt(@NotNull Instant recordedAt) {
    this.recordedAt = Objects.requireNonNull(recordedAt, "recordedAt");
  }

  public String getLanguageCode() {
    return languageCode;
  }

  public void setLanguageCode(@NotNull String languageCode) {
    this.languageCode = Objects.requireNonNull(languageCode, "languageCode");
  }

  public LocalDate getRetentionUntil() {
    return retentionUntil;
  }

  public void setRetentionUntil(@NotNull LocalDate retentionUntil) {
    this.retentionUntil = Objects.requireNonNull(retentionUntil, "retentionUntil");
  }

  public String getPledgorName() {
    return pledgorName;
  }

  public void setPledgorName(@NotNull String pledgorName) {
    this.pledgorName = Objects.requireNonNull(pledgorName, "pledgorName");
  }

  public String getPledgorStreet() {
    return pledgorStreet;
  }

  public void setPledgorStreet(String pledgorStreet) {
    this.pledgorStreet = pledgorStreet;
  }

  public String getPledgorPostalCode() {
    return pledgorPostalCode;
  }

  public void setPledgorPostalCode(String pledgorPostalCode) {
    this.pledgorPostalCode = pledgorPostalCode;
  }

  public String getPledgorCity() {
    return pledgorCity;
  }

  public void setPledgorCity(String pledgorCity) {
    this.pledgorCity = pledgorCity;
  }

  public LocalDate getPledgorBirthDate() {
    return pledgorBirthDate;
  }

  public void setPledgorBirthDate(LocalDate pledgorBirthDate) {
    this.pledgorBirthDate = pledgorBirthDate;
  }

  public String getCheckedDocumentType() {
    return checkedDocumentType;
  }

  public void setCheckedDocumentType(String checkedDocumentType) {
    this.checkedDocumentType = checkedDocumentType;
  }

  public boolean isPowerOfAttorneyRequired() {
    return powerOfAttorneyRequired;
  }

  public void setPowerOfAttorneyRequired(boolean powerOfAttorneyRequired) {
    this.powerOfAttorneyRequired = powerOfAttorneyRequired;
  }

  public String getBearerName() {
    return bearerName;
  }

  public void setBearerName(String bearerName) {
    this.bearerName = bearerName;
  }

  public String getBearerStreet() {
    return bearerStreet;
  }

  public void setBearerStreet(String bearerStreet) {
    this.bearerStreet = bearerStreet;
  }

  public String getBearerPostalCode() {
    return bearerPostalCode;
  }

  public void setBearerPostalCode(String bearerPostalCode) {
    this.bearerPostalCode = bearerPostalCode;
  }

  public String getBearerCity() {
    return bearerCity;
  }

  public void setBearerCity(String bearerCity) {
    this.bearerCity = bearerCity;
  }

  public String getPowerOfAttorneyDocumentDataUrl() {
    return powerOfAttorneyDocumentDataUrl;
  }

  public void setPowerOfAttorneyDocumentDataUrl(String powerOfAttorneyDocumentDataUrl) {
    this.powerOfAttorneyDocumentDataUrl = powerOfAttorneyDocumentDataUrl;
  }

  public Integer getSortOrder() {
    return sortOrder;
  }

  public void setSortOrder(@NotNull Integer sortOrder) {
    this.sortOrder = Objects.requireNonNull(sortOrder, "sortOrder");
  }
}
