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

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ExtendPawnTicketRequest(
    @NotNull @DecimalMin("0.01") BigDecimal outstandingLoanAmount,
    @Min(1) Integer extensionMonths,
    @DecimalMin("0.00") BigDecimal manualMonthlyOperatingFee) {}
