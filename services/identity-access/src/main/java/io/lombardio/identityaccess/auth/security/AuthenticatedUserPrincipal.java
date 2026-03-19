package io.lombardio.identityaccess.auth.security;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public record AuthenticatedUserPrincipal(
        String actorUserId,
        String userId,
        String tenantId,
        String username,
        boolean impersonating,
        String token,
        Collection<? extends GrantedAuthority> authorities
) {
}
