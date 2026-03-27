package io.lombardio.identity.aml.infrastructure.security;

import io.lombardio.platform.security.AuthenticatedUser;
import io.lombardio.platform.security.BaseAuthorizationService;
import org.springframework.stereotype.Service;

@Service
public class AmlAuthorizationService extends BaseAuthorizationService {

    public void requireRead(AuthenticatedUser user, String tenantId) {
        requireTenantAccess(user, tenantId, "aml.read", "platform.tenants.read");
    }

    public void requireWrite(AuthenticatedUser user, String tenantId) {
        requireTenantAccess(user, tenantId, "aml.write", "platform.tenants.write");
    }
}
