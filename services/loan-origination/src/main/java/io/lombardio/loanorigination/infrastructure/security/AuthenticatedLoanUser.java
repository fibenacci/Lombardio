package io.lombardio.loanorigination.infrastructure.security;

import java.util.List;

public record AuthenticatedLoanUser(
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
