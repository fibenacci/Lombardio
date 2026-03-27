package io.lombardio.platform.tenant.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record CreateTenantUserRequest(
        @NotBlank @Email String email,
        @NotBlank String password,
        @NotBlank String displayName,
        List<String> roles
) {
}
