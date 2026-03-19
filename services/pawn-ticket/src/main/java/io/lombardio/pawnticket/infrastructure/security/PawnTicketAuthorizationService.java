package io.lombardio.pawnticket.infrastructure.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class PawnTicketAuthorizationService {

    public void requireTicketWrite(AuthenticatedPawnTicketUser principal) {
        requirePermission(principal, "pawn-tickets.write");
    }

    public void requireTicketRead(AuthenticatedPawnTicketUser principal) {
        requirePermission(principal, "pawn-tickets.read");
    }

    public void requireTicketRead(AuthenticatedPawnTicketUser principal, String tenantId) {
        requireTenantAccess(principal, tenantId, "pawn-tickets.read", "platform.tenants.read");
    }

    public void requireTicketWrite(AuthenticatedPawnTicketUser principal, String tenantId) {
        requireTenantAccess(principal, tenantId, "pawn-tickets.write", "platform.tenants.write");
    }

    public void requireCashWrite(AuthenticatedPawnTicketUser principal) {
        requirePermission(principal, "cash-transactions.write");
    }

    public void requireCashRead(AuthenticatedPawnTicketUser principal) {
        requirePermission(principal, "cash-transactions.read");
    }

    public void requireCashRead(AuthenticatedPawnTicketUser principal, String tenantId) {
        requireTenantAccess(principal, tenantId, "cash-transactions.read", "platform.tenants.read");
    }

    public void requireCashWrite(AuthenticatedPawnTicketUser principal, String tenantId) {
        requireTenantAccess(principal, tenantId, "cash-transactions.write", "platform.tenants.write");
    }

    private void requireTenantAccess(
            AuthenticatedPawnTicketUser principal,
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

    private void requirePermission(AuthenticatedPawnTicketUser principal, String permission) {
        if (principal == null) {
            throw new AccessDeniedException("Authentication required");
        }
        if (!principal.hasPermission(permission)) {
            throw new AccessDeniedException("Missing permission: " + permission);
        }
    }
}
