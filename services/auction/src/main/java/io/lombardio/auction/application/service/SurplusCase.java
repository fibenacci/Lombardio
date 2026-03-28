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
package io.lombardio.auction.application.service;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SurplusCase(
    String auctionId,
    String lotId,
    int lotNumber,
    String contractNumber,
    BigDecimal hammerPrice,
    BigDecimal outstandingClaim,
    BigDecimal surplusAmount,
    LocalDate authorityTransferDueDate,
    String authorityTransferStatus) {}
