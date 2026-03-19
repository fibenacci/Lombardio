package io.lombardio.customer.infrastructure.security;

import java.util.List;

public record AuthenticatedCustomerUser(
        String userId,
        String actorUserId,
        String tenantId,
        boolean impersonating,
        List<String> permissions
) {

    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }
}
