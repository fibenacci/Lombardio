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

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record CustomerPortalPawnTicketView(
    String contractNumber,
    String ticketNumber,
    String contractBarcode,
    Instant createdAt,
    LocalDate dueDate,
    LocalDate earliestAuctionDate,
    BigDecimal loanAmount,
    BigDecimal totalRepaymentAmount,
    Integer positionCount) {}
