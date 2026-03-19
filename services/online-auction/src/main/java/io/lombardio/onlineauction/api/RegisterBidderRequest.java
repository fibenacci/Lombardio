package io.lombardio.onlineauction.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterBidderRequest(
        @NotBlank String displayName,
        @NotBlank @Email String email,
        @NotBlank String legalName,
        @NotBlank String birthDate,
        @NotBlank String iban
) {
}
