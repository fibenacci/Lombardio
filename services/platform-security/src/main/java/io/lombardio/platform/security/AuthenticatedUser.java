package io.lombardio.platform.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

public record AuthenticatedUser(
        String userId,
        String actorUserId,
        String tenantId,
        boolean impersonating,
        String email,
        String displayName,
        List<String> permissions
) {
    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }

    public static Optional<String> currentAccessToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getCredentials() instanceof String token) {
            return Optional.of(token);
        }
        // In case of standard Spring OAuth2 Resource Server, credentials might be Jwt object
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser) {
             // We need to store the token in the authentication object. 
             // Currently KeycloakJwtAuthenticationConverter uses UsernamePasswordAuthenticationToken(principal, jwt, authorities)
             // So getCredentials() should return the Jwt object.
             if (authentication.getCredentials() instanceof org.springframework.security.oauth2.jwt.Jwt jwt) {
                 return Optional.of(jwt.getTokenValue());
             }
        }
        return Optional.empty();
    }
}
