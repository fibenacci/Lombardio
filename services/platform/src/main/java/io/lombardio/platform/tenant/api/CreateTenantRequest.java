package io.lombardio.platform.tenant.api;

import jakarta.validation.constraints.NotBlank;

public record CreateTenantRequest(
        @NotBlank String key,
        @NotBlank String displayName,
        @NotBlank String status
) {
}
