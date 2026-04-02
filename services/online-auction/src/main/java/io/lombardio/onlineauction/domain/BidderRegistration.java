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
package io.lombardio.onlineauction.domain;

import java.time.Instant;

public record BidderRegistration(
    String id,
    String displayName,
    String email,
    String legalName,
    String birthDate,
    String iban,
    String paddleNumber,
    String accessToken,
    String accessTokenHash,
    BidderApprovalStatus approvalStatus,
    ReviewCheckStatus kycStatus,
    ReviewCheckStatus accountCheckStatus,
    String reviewNote,
    Instant approvedAt,
    Instant createdAt) {}
