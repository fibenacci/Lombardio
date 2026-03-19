package io.lombardio.identityaccess.access.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateRoleRequest(
        String tenantId,
        @NotBlank String key,
        @NotBlank String displayName,
        @NotBlank String description,
        boolean active,
        @NotEmpty List<String> permissionKeys
) {
}
