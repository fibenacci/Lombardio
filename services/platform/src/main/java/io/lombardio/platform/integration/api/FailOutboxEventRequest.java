package io.lombardio.platform.integration.api;

import jakarta.validation.constraints.NotBlank;

public record FailOutboxEventRequest(
        @NotBlank String consumer,
        @NotBlank String errorMessage
) {
}
