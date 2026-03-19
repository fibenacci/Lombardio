package io.lombardio.onlineauction.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class OnlineAuctionAuthorizationService {

    public void assertCanRead(AuthenticatedOnlineAuctionUser user, String tenantId) {
        assertTenantScope(user, tenantId);
        assertPermission(user, "online-auctions.read", "auctions.read");
    }

    public void assertCanWrite(AuthenticatedOnlineAuctionUser user, String tenantId) {
        assertTenantScope(user, tenantId);
        assertPermission(user, "online-auctions.write", "auctions.write");
    }

    private void assertTenantScope(AuthenticatedOnlineAuctionUser user, String tenantId) {
        if (user == null) {
            throw new AccessDeniedException("Authentication required");
        }
        if (!user.platformManager() && !tenantId.equals(user.tenantId())) {
            throw new AccessDeniedException("Tenant scope mismatch");
        }
    }

    private void assertPermission(AuthenticatedOnlineAuctionUser user, String... requiredPermissions) {
        for (String permission : requiredPermissions) {
            if (user.permissions().contains(permission)) {
                return;
            }
        }
        throw new AccessDeniedException("Missing permission");
    }
}
