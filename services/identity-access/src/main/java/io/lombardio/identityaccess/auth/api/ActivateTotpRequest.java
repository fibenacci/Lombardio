package io.lombardio.identityaccess.auth.api;

import jakarta.validation.constraints.NotBlank;

public record ActivateTotpRequest(
        @NotBlank String code
) {
}
