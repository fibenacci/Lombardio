package io.lombardio.identityaccess.access.api;

import jakarta.validation.constraints.NotBlank;

public record CreateBranchRequest(
        @NotBlank String key,
        @NotBlank String displayName,
        @NotBlank String status
) {
}
