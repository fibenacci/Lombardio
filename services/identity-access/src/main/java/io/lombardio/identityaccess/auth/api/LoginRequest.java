package io.lombardio.identityaccess.auth.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        String tenantKey,
        @NotBlank @Email String email,
        @NotBlank String password
) {
}
