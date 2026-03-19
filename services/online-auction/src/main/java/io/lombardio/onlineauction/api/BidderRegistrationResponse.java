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
        Instant createdAt
) {
}
