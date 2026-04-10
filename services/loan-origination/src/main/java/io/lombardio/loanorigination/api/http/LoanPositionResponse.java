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

@Schema(requiredProperties = {"id", "ticketGroup", "label", "description", "guidelineLabel", "baseLoanValue", "pledgedValue"})
public record LoanPositionResponse(
    @NotNull String id,
    @NotNull Integer ticketGroup,
    @NotNull String label,
    @NotNull String description,
    @NotNull String guidelineLabel,
    @NotNull BigDecimal baseLoanValue,
    @NotNull BigDecimal pledgedValue) {}
