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

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record AuctionLotRequest(
    @NotBlank String contractNumber,
    @NotBlank String itemNumber,
    @NotBlank String description,
    @NotNull @DecimalMin("0.01") BigDecimal estimatedValue,
    @NotNull @DecimalMin("0.01") BigDecimal outstandingClaim) {}
