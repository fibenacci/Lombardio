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
package io.lombardio.pawnticket.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record PawnTicket(
    String id,
    String tenantId,
    String customerId,
    String customerNumber,
    String customerDisplayName,
    String customerPhone,
    String contractNumber,
    String contractBarcode,
    String ticketNumber,
    String termsVersion,
    String termsAndConditionsText,
    Instant createdAt,
    LocalDate dueDate,
    LocalDate earliestAuctionDate,
    int termMonths,
    BigDecimal loanAmount,
    BigDecimal monthlyInterestRate,
    BigDecimal monthlyOperatingFee,
    boolean manualMonthlyOperatingFeeRequired,
    BigDecimal totalInterestAmount,
    BigDecimal totalOperatingFeeAmount,
    BigDecimal totalRepaymentAmount,
    String legalText,
    java.util.List<PawnTicketPosition> positions) {}
