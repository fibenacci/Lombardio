package io.lombardio.onlineauction.security;

import java.util.Set;

public record IdentityCurrentUser(
        String userId,
        String tenantId,
        String email,
        String displayName,
        Set<String> permissions,
        boolean platformManager
) {
}
