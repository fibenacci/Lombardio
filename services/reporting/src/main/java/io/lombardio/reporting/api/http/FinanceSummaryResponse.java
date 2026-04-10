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
package io.lombardio.reporting.api.http;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record FinanceSummaryResponse(
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal cashInflow,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal cashOutflow,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal netCashflow,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal realizedRevenue,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal activeLoanExposure,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Integer activeTicketCount,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal averageTicketValue) {}
