package io.lombardio.auction.infrastructure.security;

import org.springframework.stereotype.Service;

@Service
public class AuctionAuthorizationService {

    public void assertCanReadAuctions(AuthenticatedAuctionUser user, String tenantId) {
        assertTenantAccess(user, tenantId);
        assertPermission(user, "auctions.read");
    }

    public void assertCanWriteAuctions(AuthenticatedAuctionUser user, String tenantId) {
        assertTenantAccess(user, tenantId);
        assertPermission(user, "auctions.write");
    }

    private void assertTenantAccess(AuthenticatedAuctionUser user, String tenantId) {
        if (user == null) {
            throw new UnauthorizedIdentityAccessException("Authentication required");
        }
        if (!tenantId.equals(user.tenantId()) && !user.permissions().contains("platform.tenants.read")) {
            throw new UnauthorizedIdentityAccessException("Tenant access denied");
        }
    }

    private void assertPermission(AuthenticatedAuctionUser user, String permission) {
        if (!user.permissions().contains(permission) && !user.permissions().contains("platform.tenants.read")) {
            throw new UnauthorizedIdentityAccessException("Missing permission: " + permission);
        }
    }
}
