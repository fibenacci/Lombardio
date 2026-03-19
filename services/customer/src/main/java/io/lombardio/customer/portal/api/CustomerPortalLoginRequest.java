package io.lombardio.customer.portal.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CustomerPortalLoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password
) {
}
