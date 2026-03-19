package io.lombardio.identityaccess.auth.api;

import jakarta.validation.constraints.NotBlank;

public record VerifyTotpChallengeRequest(
        @NotBlank String challengeId,
        @NotBlank String code
) {
}
