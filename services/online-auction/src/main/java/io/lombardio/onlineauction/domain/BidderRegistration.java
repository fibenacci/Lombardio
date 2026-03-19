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
        BidderApprovalStatus approvalStatus,
        ReviewCheckStatus kycStatus,
        ReviewCheckStatus accountCheckStatus,
        String reviewNote,
        Instant approvedAt,
        Instant createdAt
) {
}
