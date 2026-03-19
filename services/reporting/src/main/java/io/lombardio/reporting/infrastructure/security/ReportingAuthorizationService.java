package io.lombardio.reporting.infrastructure.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class ReportingAuthorizationService {

    public void requireRead(AuthenticatedReportingUser principal, String tenantId) {
        requireTenantAccess(principal, tenantId, "reporting.read", "platform.tenants.read");
    }

    private void requireTenantAccess(
            AuthenticatedReportingUser principal,
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
