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
package io.lombardio.loanorigination.api.http;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record PawnTicketResponse(
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
    List<PawnTicketPositionResponse> positions) {

  public PawnTicketResponse {
    positions = List.copyOf(positions == null ? List.of() : positions);
  }

  @Override
  public List<PawnTicketPositionResponse> positions() {
    return List.copyOf(positions);
  }
}
