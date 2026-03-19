package io.lombardio.onlineauction.api;

import jakarta.validation.constraints.NotBlank;

public record BidderReviewRequest(
        @NotBlank String kycStatus,
        @NotBlank String accountCheckStatus,
        @NotBlank String decision,
        String reviewNote
) {
}
