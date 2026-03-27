package io.lombardio.auction.infrastructure.security;

import io.lombardio.platform.security.AuthenticatedUser;
import io.lombardio.platform.security.BaseAuthorizationService;
import org.springframework.stereotype.Service;

@Service
public class AuctionAuthorizationService extends BaseAuthorizationService {

    public void requireRead(AuthenticatedUser user, String tenantId) {
        requireTenantAccess(user, tenantId, "auctions.read", "platform.tenants.read");
    }

    public void requireWrite(AuthenticatedUser user, String tenantId) {
        requireTenantAccess(user, tenantId, "auctions.write", "platform.tenants.write");
    }
}
