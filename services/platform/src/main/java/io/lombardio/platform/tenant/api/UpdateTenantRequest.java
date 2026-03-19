package io.lombardio.platform.tenant.api;

import jakarta.validation.constraints.NotBlank;

public record UpdateTenantRequest(
        @NotBlank String key,
        @NotBlank String displayName,
        @NotBlank String status
) {
}
