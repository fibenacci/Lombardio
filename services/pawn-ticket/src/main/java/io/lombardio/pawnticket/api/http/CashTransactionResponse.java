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
import java.math.BigDecimal;
import java.time.Instant;

public record CashTransactionResponse(
    String id,
    String ticketNumber,
    String customerNumber,
    String customerDisplayName,
    CashTransactionType type,
    BigDecimal outstandingLoanAmount,
    BigDecimal interestAmount,
    BigDecimal operatingFeeAmount,
    BigDecimal totalAmount,
    String legalText,
    String note,
    Instant createdAt) {}
