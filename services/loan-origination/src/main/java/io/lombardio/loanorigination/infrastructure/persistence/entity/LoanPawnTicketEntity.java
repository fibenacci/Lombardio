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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "loan_pawn_tickets", schema = "loan_origination")
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

  public LoanCaseEntity getLoanCase() {
    return loanCase;
  }

  public void setLoanCase(LoanCaseEntity loanCase) {
    this.loanCase = loanCase;
  }

  public String getContractNumber() {
    return contractNumber;
  }

  public void setContractNumber(String contractNumber) {
    this.contractNumber = contractNumber;
  }

  public String getContractBarcode() {
    return contractBarcode;
  }

  public void setContractBarcode(String contractBarcode) {
    this.contractBarcode = contractBarcode;
  }

  public String getTicketNumber() {
    return ticketNumber;
  }

  public void setTicketNumber(String ticketNumber) {
    this.ticketNumber = ticketNumber;
  }

  public String getTermsVersion() {
    return termsVersion;
  }

  public void setTermsVersion(String termsVersion) {
    this.termsVersion = termsVersion;
  }

  public String getTermsAndConditionsText() {
    return termsAndConditionsText;
  }

  public void setTermsAndConditionsText(String termsAndConditionsText) {
    this.termsAndConditionsText = termsAndConditionsText;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDate getDueDate() {
    return dueDate;
  }

  public void setDueDate(LocalDate dueDate) {
    this.dueDate = dueDate;
  }

  public LocalDate getEarliestAuctionDate() {
    return earliestAuctionDate;
  }

  public void setEarliestAuctionDate(LocalDate earliestAuctionDate) {
    this.earliestAuctionDate = earliestAuctionDate;
  }

  public Integer getTermMonths() {
    return termMonths;
  }

  public void setTermMonths(Integer termMonths) {
    this.termMonths = termMonths;
  }

  public BigDecimal getTotalLoanValue() {
    return totalLoanValue;
  }

  public void setTotalLoanValue(BigDecimal totalLoanValue) {
    this.totalLoanValue = totalLoanValue;
  }

  public BigDecimal getMonthlyInterestRate() {
    return monthlyInterestRate;
  }

  public void setMonthlyInterestRate(BigDecimal monthlyInterestRate) {
    this.monthlyInterestRate = monthlyInterestRate;
  }

  public BigDecimal getMonthlyOperatingFee() {
    return monthlyOperatingFee;
  }

  public void setMonthlyOperatingFee(BigDecimal monthlyOperatingFee) {
    this.monthlyOperatingFee = monthlyOperatingFee;
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

  public void setTotalInterestAmount(BigDecimal totalInterestAmount) {
    this.totalInterestAmount = totalInterestAmount;
  }

  public BigDecimal getTotalOperatingFeeAmount() {
    return totalOperatingFeeAmount;
  }

  public void setTotalOperatingFeeAmount(BigDecimal totalOperatingFeeAmount) {
    this.totalOperatingFeeAmount = totalOperatingFeeAmount;
  }

  public BigDecimal getTotalRepaymentAmount() {
    return totalRepaymentAmount;
  }

  public void setTotalRepaymentAmount(BigDecimal totalRepaymentAmount) {
    this.totalRepaymentAmount = totalRepaymentAmount;
  }

  public String getLegalText() {
    return legalText;
  }

  public void setLegalText(String legalText) {
    this.legalText = legalText;
  }

  public Integer getSortOrder() {
    return sortOrder;
  }

  public void setSortOrder(Integer sortOrder) {
    this.sortOrder = sortOrder;
  }
}
