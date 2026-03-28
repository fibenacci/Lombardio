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

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record PawnTicketOverviewResponse(
    String contractNumber,
    String ticketNumber,
    String contractBarcode,
    String termsVersion,
    String customerNumber,
    String customerDisplayName,
    Instant createdAt,
    LocalDate dueDate,
    LocalDate earliestAuctionDate,
    BigDecimal totalLoanValue,
    BigDecimal totalRepaymentAmount,
    Integer positionCount) {}
