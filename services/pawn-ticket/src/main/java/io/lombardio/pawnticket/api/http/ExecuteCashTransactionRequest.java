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
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record ExecuteCashTransactionRequest(
    @NotBlank String tenantId,
    @NotBlank String ticketNumber,
    @NotNull CashTransactionType type,
    @NotNull @DecimalMin("0.01") BigDecimal outstandingLoanAmount,
    @Positive Integer extensionMonths,
    @DecimalMin("0.01") BigDecimal repaymentAmount,
    Integer remainingTermMonths,
    @DecimalMin("0.00") BigDecimal manualMonthlyOperatingFee,
    String note) {}
