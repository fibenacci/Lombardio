package io.lombardio.platform.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class PlatformAuthorizationService {

    public void requirePlatformRead(AuthenticatedPlatformUser principal) {
        requireAuthenticated(principal);
        requireNotImpersonating(principal);
        requirePermission(principal, "platform.tenants.read");
    }

    public void requireTenantFeatureRead(AuthenticatedPlatformUser principal, String tenantId) {
        requireAuthenticated(principal);

        if (principal.hasPermission("platform.tenants.read")) {
            return;
        }

        if (!tenantId.equals(principal.tenantId())) {
            throw new AccessDeniedException("Tenant feature access is limited to the effective tenant");
        }
    }

    public void requirePlatformWrite(AuthenticatedPlatformUser principal) {
        requireAuthenticated(principal);
        requireNotImpersonating(principal);
        requirePermission(principal, "platform.tenants.write");
    }

    private void requireAuthenticated(AuthenticatedPlatformUser principal) {
        if (principal == null) {
            throw new AccessDeniedException("Authentication required");
        }
    }

    private void requireNotImpersonating(AuthenticatedPlatformUser principal) {
        if (principal.impersonating()) {
            throw new AccessDeniedException("Delegated sessions may not manage platform resources");
        }
    }

    private void requirePermission(AuthenticatedPlatformUser principal, String permission) {
        if (!principal.hasPermission(permission)) {
            throw new AccessDeniedException("Missing permission: " + permission);
        }
    }
}
