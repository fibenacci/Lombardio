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

import io.lombardio.pawnticket.domain.model.CashTransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;

@Schema(
    requiredProperties = {
      "id",
      "ticketNumber",
      "customerNumber",
      "customerDisplayName",
      "type",
      "outstandingLoanAmount",
      "interestAmount",
      "operatingFeeAmount",
      "totalAmount",
      "legalText",
      "note",
      "createdAt"
    })
public record CashTransactionResponse(
    @NotNull String id,
    @NotNull String ticketNumber,
    @NotNull String customerNumber,
    @NotNull String customerDisplayName,
    @NotNull CashTransactionType type,
    @NotNull BigDecimal outstandingLoanAmount,
    @NotNull BigDecimal interestAmount,
    @NotNull BigDecimal operatingFeeAmount,
    @NotNull BigDecimal totalAmount,
    @NotNull String legalText,
    @NotNull String note,
    @NotNull Instant createdAt) {}
