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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.lombardio.pawnticket.domain.model.CashTransactionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cash_transactions")
@Getter
@Setter
@NoArgsConstructor
@SuppressFBWarnings(
    value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"},
    justification = "JPA entities")
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
}
