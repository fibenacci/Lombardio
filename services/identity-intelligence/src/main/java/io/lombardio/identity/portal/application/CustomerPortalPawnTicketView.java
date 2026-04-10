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
package io.lombardio.identity.portal.application;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record CustomerPortalPawnTicketView(
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String contractNumber,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String ticketNumber,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String contractBarcode,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant createdAt,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) LocalDate dueDate,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) LocalDate earliestAuctionDate,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal loanAmount,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal totalRepaymentAmount,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Integer positionCount) {}
