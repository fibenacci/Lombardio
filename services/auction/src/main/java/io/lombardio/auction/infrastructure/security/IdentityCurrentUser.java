package io.lombardio.auction.infrastructure.security;

import java.util.List;

public record IdentityCurrentUser(
        String userId,
        String tenantId,
        String email,
        String displayName,
        List<String> permissions
) {
}
