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
import java.time.Instant;

public record BidderRegistrationResponse(
    String id,
    String displayName,
    String email,
    String legalName,
    String birthDate,
    String ibanMasked,
    String paddleNumber,
    String accessToken,
    BidderApprovalStatus approvalStatus,
    ReviewCheckStatus kycStatus,
    ReviewCheckStatus accountCheckStatus,
    String reviewNote,
    Instant approvedAt,
    Instant createdAt) {}
