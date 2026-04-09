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

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Schema(requiredProperties = {
    "contractNumber", "contractBarcode", "ticketNumber", "termsVersion", "termsAndConditionsText",
    "createdAt", "dueDate", "earliestAuctionDate", "termMonths", "totalLoanValue",
    "monthlyInterestRate", "monthlyOperatingFee", "manualMonthlyOperatingFeeRequired",
    "totalInterestAmount", "totalOperatingFeeAmount", "totalRepaymentAmount", "legalText", "positions"
})
public record PawnTicketResponse(
    @NotNull String contractNumber,
    @NotNull String contractBarcode,
    @NotNull String ticketNumber,
    @NotNull String termsVersion,
    @NotNull String termsAndConditionsText,
    @NotNull Instant createdAt,
    @NotNull LocalDate dueDate,
    @NotNull LocalDate earliestAuctionDate,
    @NotNull Integer termMonths,
    @NotNull BigDecimal totalLoanValue,
    @NotNull BigDecimal monthlyInterestRate,
    @NotNull BigDecimal monthlyOperatingFee,
    @NotNull boolean manualMonthlyOperatingFeeRequired,
    @NotNull BigDecimal totalInterestAmount,
    @NotNull BigDecimal totalOperatingFeeAmount,
    @NotNull BigDecimal totalRepaymentAmount,
    @NotNull String legalText,
    @NotNull List<PawnTicketPositionResponse> positions) {

  public PawnTicketResponse {
    positions = List.copyOf(positions == null ? List.of() : positions);
  }

  @Override
  public List<PawnTicketPositionResponse> positions() {
    return List.copyOf(positions);
  }
}
