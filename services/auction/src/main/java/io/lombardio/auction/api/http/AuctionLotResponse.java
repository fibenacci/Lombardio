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
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(requiredProperties = {
    "id", "lotNumber", "contractNumber", "itemNumber", "description",
    "estimatedValue", "outstandingClaim", "latestBidAmount", "leadingBidder",
    "hammerPrice", "status", "surplusAmount", "authorityTransferDueDate",
    "authorityTransferStatus"
})
public record AuctionLotResponse(
    @NotNull String id,
    @NotNull int lotNumber,
    @NotNull String contractNumber,
    @NotNull String itemNumber,
    @NotNull String description,
    @NotNull BigDecimal estimatedValue,
    @NotNull BigDecimal outstandingClaim,
    @NotNull BigDecimal latestBidAmount,
    @NotNull String leadingBidder,
    @NotNull BigDecimal hammerPrice,
    @NotNull AuctionLotStatus status,
    @NotNull BigDecimal surplusAmount,
    @NotNull LocalDate authorityTransferDueDate,
    @NotNull String authorityTransferStatus) {}
