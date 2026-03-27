package io.lombardio.identity.kyc.infrastructure.security;

import io.lombardio.platform.security.AuthenticatedUser;
import io.lombardio.platform.security.BaseAuthorizationService;
import org.springframework.stereotype.Service;

@Service
public class KycAuthorizationService extends BaseAuthorizationService {

    public void requireRead(AuthenticatedUser user, String tenantId) {
        requireTenantAccess(user, tenantId, "kyc.read", "platform.tenants.read");
    }

    public void requireWrite(AuthenticatedUser user, String tenantId) {
        requireTenantAccess(user, tenantId, "kyc.write", "platform.tenants.write");
    }
}
