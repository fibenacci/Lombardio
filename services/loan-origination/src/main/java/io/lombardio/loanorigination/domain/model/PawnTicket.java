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
package io.lombardio.loanorigination.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record PawnTicket(
    String id,
    String contractNumber,
    String contractBarcode,
    String ticketNumber,
    String termsVersion,
    String termsAndConditionsText,
    Instant createdAt,
    LocalDate dueDate,
    LocalDate earliestAuctionDate,
    Integer termMonths,
    BigDecimal totalLoanValue,
    BigDecimal monthlyInterestRate,
    BigDecimal monthlyOperatingFee,
    boolean manualMonthlyOperatingFeeRequired,
    BigDecimal totalInterestAmount,
    BigDecimal totalOperatingFeeAmount,
    BigDecimal totalRepaymentAmount,
    String legalText,
    java.util.List<PawnTicketPosition> positions) {

  public PawnTicket {
    positions = java.util.List.copyOf(positions != null ? positions : java.util.List.of());
  }
}
