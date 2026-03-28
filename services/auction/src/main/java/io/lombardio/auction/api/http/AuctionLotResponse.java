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
package io.lombardio.auction.api.http;

import io.lombardio.auction.domain.model.AuctionLotStatus;
import java.math.BigDecimal;
import java.time.LocalDate;

public record AuctionLotResponse(
    String id,
    int lotNumber,
    String contractNumber,
    String itemNumber,
    String description,
    BigDecimal estimatedValue,
    BigDecimal outstandingClaim,
    BigDecimal latestBidAmount,
    String leadingBidder,
    BigDecimal hammerPrice,
    AuctionLotStatus status,
    BigDecimal surplusAmount,
    LocalDate authorityTransferDueDate,
    String authorityTransferStatus) {}
