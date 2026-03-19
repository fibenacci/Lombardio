package io.lombardio.platform.integration.api;

import jakarta.validation.constraints.NotBlank;

public record CompleteOutboxEventRequest(
        @NotBlank String consumer
) {
}
