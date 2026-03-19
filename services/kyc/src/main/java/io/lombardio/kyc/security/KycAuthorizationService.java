package io.lombardio.kyc.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class KycAuthorizationService {

    public void requireRead(AuthenticatedKycUser principal, String tenantId) {
        requireTenantAccess(principal, tenantId, "kyc.read", "platform.tenants.read");
    }

    public void requireWrite(AuthenticatedKycUser principal, String tenantId) {
        requireTenantAccess(principal, tenantId, "kyc.write", "platform.tenants.write");
    }

    private void requireTenantAccess(
            AuthenticatedKycUser principal,
            String tenantId,
            String permission,
            String crossTenantPermission
    ) {
        if (principal == null) {
            throw new AccessDeniedException("Authentication required");
        }
        if (!principal.hasPermission(permission)) {
            throw new AccessDeniedException("Missing permission: " + permission);
        }
        if (tenantId.equals(principal.tenantId())) {
            return;
        }
        if (principal.hasPermission(crossTenantPermission)) {
            return;
        }
        throw new AccessDeniedException("Tenant access is limited to the effective tenant");
    }
}
