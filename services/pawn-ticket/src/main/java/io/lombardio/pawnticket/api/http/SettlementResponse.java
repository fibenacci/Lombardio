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

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(
    requiredProperties = {
      "outstandingLoanAmount",
      "interestAmount",
      "operatingFeeAmount",
      "totalDueAmount",
      "legalText"
    })
public record SettlementResponse(
    @NotNull BigDecimal outstandingLoanAmount,
    @NotNull BigDecimal interestAmount,
    @NotNull BigDecimal operatingFeeAmount,
    @NotNull BigDecimal totalDueAmount,
    @NotNull String legalText) {}
