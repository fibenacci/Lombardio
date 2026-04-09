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
import java.time.Instant;
import java.time.LocalDate;

@Schema(
    requiredProperties = {
      "contractNumber",
      "ticketNumber",
      "contractBarcode",
      "termsVersion",
      "customerNumber",
      "customerDisplayName",
      "createdAt",
      "dueDate",
      "earliestAuctionDate",
      "totalLoanValue",
      "totalRepaymentAmount",
      "positionCount"
    })
public record PawnTicketOverviewResponse(
    @NotNull String contractNumber,
    @NotNull String ticketNumber,
    @NotNull String contractBarcode,
    @NotNull String termsVersion,
    @NotNull String customerNumber,
    @NotNull String customerDisplayName,
    @NotNull Instant createdAt,
    @NotNull LocalDate dueDate,
    @NotNull LocalDate earliestAuctionDate,
    @NotNull BigDecimal totalLoanValue,
    @NotNull BigDecimal totalRepaymentAmount,
    @NotNull Integer positionCount) {}
