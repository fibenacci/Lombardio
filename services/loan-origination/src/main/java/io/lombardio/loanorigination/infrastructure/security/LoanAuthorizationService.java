package io.lombardio.loanorigination.infrastructure.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class LoanAuthorizationService {

    public void requireRead(AuthenticatedLoanUser principal, String tenantId) {
        requireTenantAccess(principal, tenantId, "loans.read", "platform.tenants.read");
    }

    public void requireWrite(AuthenticatedLoanUser principal, String tenantId) {
        requireTenantAccess(principal, tenantId, "loans.write", "platform.tenants.write");
    }

    private void requireTenantAccess(
            AuthenticatedLoanUser principal,
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
