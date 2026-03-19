package io.lombardio.platform.integration.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ClaimOutboxEventsRequest(
        @NotBlank String consumer,
        @Min(1) @Max(100) int limit
) {
}
