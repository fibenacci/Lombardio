package io.lombardio.aml.infrastructure.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class AmlAuthorizationService {

    public void requireRead(AuthenticatedAmlUser principal, String tenantId) {
        requireTenantAccess(principal, tenantId, "aml.read", "platform.tenants.read");
    }

    public void requireWrite(AuthenticatedAmlUser principal, String tenantId) {
        requireTenantAccess(principal, tenantId, "aml.write", "platform.tenants.write");
    }

    private void requireTenantAccess(
            AuthenticatedAmlUser principal,
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
