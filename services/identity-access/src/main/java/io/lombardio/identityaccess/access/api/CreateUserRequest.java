package io.lombardio.identityaccess.access.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateUserRequest(
        String tenantId,
        List<String> branchIds,
        @NotBlank String username,
        @NotBlank @Email String email,
        @NotBlank String initialPassword,
        @NotBlank String displayName,
        @NotBlank String status,
        @NotEmpty List<String> roleIds
) {
}
