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
package io.lombardio.onlineauction.api;

import io.lombardio.onlineauction.domain.BidderApprovalStatus;
import io.lombardio.onlineauction.domain.ReviewCheckStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

@Schema(
    requiredProperties = {
      "id",
      "displayName",
      "email",
      "legalName",
      "birthDate",
      "ibanMasked",
      "paddleNumber",
      "accessToken",
      "approvalStatus",
      "kycStatus",
      "accountCheckStatus",
      "reviewNote",
      "approvedAt",
      "createdAt"
    })
public record BidderRegistrationResponse(
    @NotNull String id,
    @NotNull String displayName,
    @NotNull String email,
    @NotNull String legalName,
    @NotNull String birthDate,
    @NotNull String ibanMasked,
    @NotNull String paddleNumber,
    @NotNull String accessToken,
    @NotNull BidderApprovalStatus approvalStatus,
    @NotNull ReviewCheckStatus kycStatus,
    @NotNull ReviewCheckStatus accountCheckStatus,
    @NotNull String reviewNote,
    @NotNull Instant approvedAt,
    @NotNull Instant createdAt) {}
