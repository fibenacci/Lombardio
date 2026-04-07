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
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "loan_pawn_tickets")
@Getter
@Setter
@NoArgsConstructor
@SuppressFBWarnings(
    value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"},
    justification = "JPA entities")
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
}
