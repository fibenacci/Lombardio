package io.lombardio.identityaccess.auth.api;

import jakarta.validation.constraints.NotBlank;

public record CreateDelegationRequest(
        @NotBlank String userId
) {
}
