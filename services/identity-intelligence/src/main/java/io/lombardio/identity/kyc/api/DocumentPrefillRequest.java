package io.lombardio.identity.kyc.api;

import jakarta.validation.constraints.NotBlank;

public record DocumentPrefillRequest(
        @NotBlank String documentFrontImageDataUrl,
        @NotBlank String documentBackImageDataUrl
) {
}
