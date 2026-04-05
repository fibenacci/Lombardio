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
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "loan_pawn_tickets")
public class LoanPawnTicketEntity {

  @Id private String id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "loan_case_id", nullable = false)
  private LoanCaseEntity loanCase;

  @Column(name = "contract_number", nullable = false)
  private String contractNumber;

  @Column(name = "contract_barcode", nullable = false)
  private String contractBarcode;

  @Column(name = "ticket_number", nullable = false)
  private String ticketNumber;

  @Column(name = "terms_version", nullable = false)
  private String termsVersion;

  @Column(name = "terms_and_conditions_text", nullable = false, columnDefinition = "text")
  private String termsAndConditionsText;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "due_date", nullable = false)
  private LocalDate dueDate;

  @Column(name = "earliest_auction_date", nullable = false)
  private LocalDate earliestAuctionDate;

  @Column(name = "term_months", nullable = false)
  private Integer termMonths;

  @Column(name = "total_loan_value", nullable = false)
  private BigDecimal totalLoanValue;

  @Column(name = "monthly_interest_rate", nullable = false)
  private BigDecimal monthlyInterestRate;

  @Column(name = "monthly_operating_fee", nullable = false)
  private BigDecimal monthlyOperatingFee;

  @Column(name = "manual_monthly_operating_fee_required", nullable = false)
  private boolean manualMonthlyOperatingFeeRequired;

  @Column(name = "total_interest_amount", nullable = false)
  private BigDecimal totalInterestAmount;

  @Column(name = "total_operating_fee_amount", nullable = false)
  private BigDecimal totalOperatingFeeAmount;

  @Column(name = "total_repayment_amount", nullable = false)
  private BigDecimal totalRepaymentAmount;

  @Column(name = "legal_text", nullable = false, columnDefinition = "text")
  private String legalText;

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

  public String getContractNumber() {
    return contractNumber;
  }

  public void setContractNumber(@NotNull String contractNumber) {
    this.contractNumber = Objects.requireNonNull(contractNumber, "contractNumber");
  }

  public String getContractBarcode() {
    return contractBarcode;
  }

  public void setContractBarcode(@NotNull String contractBarcode) {
    this.contractBarcode = Objects.requireNonNull(contractBarcode, "contractBarcode");
  }

  public String getTicketNumber() {
    return ticketNumber;
  }

  public void setTicketNumber(@NotNull String ticketNumber) {
    this.ticketNumber = Objects.requireNonNull(ticketNumber, "ticketNumber");
  }

  public String getTermsVersion() {
    return termsVersion;
  }

  public void setTermsVersion(@NotNull String termsVersion) {
    this.termsVersion = Objects.requireNonNull(termsVersion, "termsVersion");
  }

  public String getTermsAndConditionsText() {
    return termsAndConditionsText;
  }

  public void setTermsAndConditionsText(@NotNull String termsAndConditionsText) {
    this.termsAndConditionsText =
        Objects.requireNonNull(termsAndConditionsText, "termsAndConditionsText");
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(@NotNull Instant createdAt) {
    this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
  }

  public LocalDate getDueDate() {
    return dueDate;
  }

  public void setDueDate(@NotNull LocalDate dueDate) {
    this.dueDate = Objects.requireNonNull(dueDate, "dueDate");
  }

  public LocalDate getEarliestAuctionDate() {
    return earliestAuctionDate;
  }

  public void setEarliestAuctionDate(@NotNull LocalDate earliestAuctionDate) {
    this.earliestAuctionDate = Objects.requireNonNull(earliestAuctionDate, "earliestAuctionDate");
  }

  public Integer getTermMonths() {
    return termMonths;
  }

  public void setTermMonths(@NotNull Integer termMonths) {
    this.termMonths = Objects.requireNonNull(termMonths, "termMonths");
  }

  public BigDecimal getTotalLoanValue() {
    return totalLoanValue;
  }

  public void setTotalLoanValue(@NotNull BigDecimal totalLoanValue) {
    this.totalLoanValue = Objects.requireNonNull(totalLoanValue, "totalLoanValue");
  }

  public BigDecimal getMonthlyInterestRate() {
    return monthlyInterestRate;
  }

  public void setMonthlyInterestRate(@NotNull BigDecimal monthlyInterestRate) {
    this.monthlyInterestRate = Objects.requireNonNull(monthlyInterestRate, "monthlyInterestRate");
  }

  public BigDecimal getMonthlyOperatingFee() {
    return monthlyOperatingFee;
  }

  public void setMonthlyOperatingFee(@NotNull BigDecimal monthlyOperatingFee) {
    this.monthlyOperatingFee = Objects.requireNonNull(monthlyOperatingFee, "monthlyOperatingFee");
  }

  public boolean isManualMonthlyOperatingFeeRequired() {
    return manualMonthlyOperatingFeeRequired;
  }

  public void setManualMonthlyOperatingFeeRequired(boolean manualMonthlyOperatingFeeRequired) {
    this.manualMonthlyOperatingFeeRequired = manualMonthlyOperatingFeeRequired;
  }

  public BigDecimal getTotalInterestAmount() {
    return totalInterestAmount;
  }

  public void setTotalInterestAmount(@NotNull BigDecimal totalInterestAmount) {
    this.totalInterestAmount = Objects.requireNonNull(totalInterestAmount, "totalInterestAmount");
  }

  public BigDecimal getTotalOperatingFeeAmount() {
    return totalOperatingFeeAmount;
  }

  public void setTotalOperatingFeeAmount(@NotNull BigDecimal totalOperatingFeeAmount) {
    this.totalOperatingFeeAmount =
        Objects.requireNonNull(totalOperatingFeeAmount, "totalOperatingFeeAmount");
  }

  public BigDecimal getTotalRepaymentAmount() {
    return totalRepaymentAmount;
  }

  public void setTotalRepaymentAmount(@NotNull BigDecimal totalRepaymentAmount) {
    this.totalRepaymentAmount =
        Objects.requireNonNull(totalRepaymentAmount, "totalRepaymentAmount");
  }

  public String getLegalText() {
    return legalText;
  }

  public void setLegalText(@NotNull String legalText) {
    this.legalText = Objects.requireNonNull(legalText, "legalText");
  }

  public Integer getSortOrder() {
    return sortOrder;
  }

  public void setSortOrder(@NotNull Integer sortOrder) {
    this.sortOrder = Objects.requireNonNull(sortOrder, "sortOrder");
  }
}
