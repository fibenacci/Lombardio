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

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(requiredProperties = {
    "auctionId", "lotId", "lotNumber", "contractNumber", "hammerPrice",
    "outstandingClaim", "surplusAmount", "authorityTransferDueDate",
    "authorityTransferStatus"
})
public record SurplusCaseResponse(
    @NotNull String auctionId,
    @NotNull String lotId,
    @NotNull int lotNumber,
    @NotNull String contractNumber,
    @NotNull BigDecimal hammerPrice,
    @NotNull BigDecimal outstandingClaim,
    @NotNull BigDecimal surplusAmount,
    @NotNull LocalDate authorityTransferDueDate,
    @NotNull String authorityTransferStatus) {}
