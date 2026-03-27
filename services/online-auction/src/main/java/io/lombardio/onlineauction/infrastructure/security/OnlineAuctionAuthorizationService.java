package io.lombardio.onlineauction.infrastructure.security;

import io.lombardio.platform.security.AuthenticatedUser;
import io.lombardio.platform.security.BaseAuthorizationService;
import org.springframework.stereotype.Service;

@Service
public class OnlineAuctionAuthorizationService extends BaseAuthorizationService {

    public void requireRead(AuthenticatedUser user, String tenantId) {
        requireTenantAccess(user, tenantId, "online-auctions.read", "platform.tenants.read");
    }

    public void requireWrite(AuthenticatedUser user, String tenantId) {
        requireTenantAccess(user, tenantId, "online-auctions.write", "platform.tenants.write");
    }
}
