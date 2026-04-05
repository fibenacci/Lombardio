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
package io.lombardio.pawnticket.api.http;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public record IssuePawnTicketRequest(
    @NotBlank String tenantId,
    @NotBlank String customerId,
    @NotBlank String customerNumber,
    @NotBlank String customerDisplayName,
    String customerPhone,
    @NotNull @DecimalMin("0.01") BigDecimal loanAmount,
    @Min(3) Integer termMonths,
    @DecimalMin("0.00") BigDecimal manualMonthlyOperatingFee,
    @NotEmpty List<@Valid PawnTicketPositionPayload> positions) {

  public IssuePawnTicketRequest {
    positions = List.copyOf(positions == null ? List.of() : positions);
  }

  @Override
  public List<PawnTicketPositionPayload> positions() {
    return List.copyOf(positions);
  }
}
