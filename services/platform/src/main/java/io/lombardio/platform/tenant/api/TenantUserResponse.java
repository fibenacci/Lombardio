package io.lombardio.platform.tenant.api;

import java.util.List;

public record TenantUserResponse(
        String id,
        String email,
        String displayName,
        List<String> roles
) {
}
