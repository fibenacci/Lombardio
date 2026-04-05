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
package io.lombardio.pawnticket.infrastructure.persistence.entity;

import io.lombardio.pawnticket.domain.model.CashTransactionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "cash_transactions")
public class CashTransactionEntity {

  @Id private String id;

  @Column(name = "tenant_id", nullable = false)
  private String tenantId;

  @Column(name = "ticket_number", nullable = false)
  private String ticketNumber;

  @Column(name = "customer_number", nullable = false)
  private String customerNumber;

  @Column(name = "customer_display_name", nullable = false)
  private String customerDisplayName;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false)
  private CashTransactionType type;

  @Column(name = "outstanding_loan_amount", nullable = false)
  private BigDecimal outstandingLoanAmount;

  @Column(name = "interest_amount", nullable = false)
  private BigDecimal interestAmount;

  @Column(name = "operating_fee_amount", nullable = false)
  private BigDecimal operatingFeeAmount;

  @Column(name = "total_amount", nullable = false)
  private BigDecimal totalAmount;

  @Column(name = "legal_text", nullable = false, columnDefinition = "text")
  private String legalText;

  @Column(name = "note")
  private String note;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(@NotNull String tenantId) {
    this.tenantId = Objects.requireNonNull(tenantId, "tenantId");
  }

  public String getTicketNumber() {
    return ticketNumber;
  }

  public void setTicketNumber(@NotNull String ticketNumber) {
    this.ticketNumber = Objects.requireNonNull(ticketNumber, "ticketNumber");
  }

  public String getCustomerNumber() {
    return customerNumber;
  }

  public void setCustomerNumber(@NotNull String customerNumber) {
    this.customerNumber = Objects.requireNonNull(customerNumber, "customerNumber");
  }

  public String getCustomerDisplayName() {
    return customerDisplayName;
  }

  public void setCustomerDisplayName(@NotNull String customerDisplayName) {
    this.customerDisplayName = Objects.requireNonNull(customerDisplayName, "customerDisplayName");
  }

  public CashTransactionType getType() {
    return type;
  }

  public void setType(@NotNull CashTransactionType type) {
    this.type = Objects.requireNonNull(type, "type");
  }

  public BigDecimal getOutstandingLoanAmount() {
    return outstandingLoanAmount;
  }

  public void setOutstandingLoanAmount(@NotNull BigDecimal outstandingLoanAmount) {
    this.outstandingLoanAmount =
        Objects.requireNonNull(outstandingLoanAmount, "outstandingLoanAmount");
  }

  public BigDecimal getInterestAmount() {
    return interestAmount;
  }

  public void setInterestAmount(@NotNull BigDecimal interestAmount) {
    this.interestAmount = Objects.requireNonNull(interestAmount, "interestAmount");
  }

  public BigDecimal getOperatingFeeAmount() {
    return operatingFeeAmount;
  }

  public void setOperatingFeeAmount(@NotNull BigDecimal operatingFeeAmount) {
    this.operatingFeeAmount = Objects.requireNonNull(operatingFeeAmount, "operatingFeeAmount");
  }

  public BigDecimal getTotalAmount() {
    return totalAmount;
  }

  public void setTotalAmount(@NotNull BigDecimal totalAmount) {
    this.totalAmount = Objects.requireNonNull(totalAmount, "totalAmount");
  }

  public String getLegalText() {
    return legalText;
  }

  public void setLegalText(@NotNull String legalText) {
    this.legalText = Objects.requireNonNull(legalText, "legalText");
  }

  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(@NotNull Instant createdAt) {
    this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
  }
}
