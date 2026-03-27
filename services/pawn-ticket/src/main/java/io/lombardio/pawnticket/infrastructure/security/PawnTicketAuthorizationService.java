package io.lombardio.pawnticket.infrastructure.security;

import io.lombardio.platform.security.AuthenticatedUser;
import io.lombardio.platform.security.BaseAuthorizationService;
import org.springframework.stereotype.Service;

@Service
public class PawnTicketAuthorizationService extends BaseAuthorizationService {

    public void requireTicketWrite(AuthenticatedUser principal) {
        requirePermission(principal, "pawn-tickets.write");
    }

    public void requireTicketRead(AuthenticatedUser principal) {
        requirePermission(principal, "pawn-tickets.read");
    }

    public void requireTicketRead(AuthenticatedUser principal, String tenantId) {
        requireTenantAccess(principal, tenantId, "pawn-tickets.read", "platform.tenants.read");
    }

    public void requireTicketWrite(AuthenticatedUser principal, String tenantId) {
        requireTenantAccess(principal, tenantId, "pawn-tickets.write", "platform.tenants.write");
    }

    public void requireCashWrite(AuthenticatedUser principal) {
        requirePermission(principal, "cash-transactions.write");
    }

    public void requireCashRead(AuthenticatedUser principal) {
        requirePermission(principal, "cash-transactions.read");
    }

    public void requireCashRead(AuthenticatedUser principal, String tenantId) {
        requireTenantAccess(principal, tenantId, "cash-transactions.read", "platform.tenants.read");
    }

    public void requireCashWrite(AuthenticatedUser principal, String tenantId) {
        requireTenantAccess(principal, tenantId, "cash-transactions.write", "platform.tenants.write");
    }
}
