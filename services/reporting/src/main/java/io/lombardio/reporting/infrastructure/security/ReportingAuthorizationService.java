package io.lombardio.reporting.infrastructure.security;

import io.lombardio.platform.security.AuthenticatedUser;
import io.lombardio.platform.security.BaseAuthorizationService;
import org.springframework.stereotype.Service;

@Service
public class ReportingAuthorizationService extends BaseAuthorizationService {

    public void requireRead(AuthenticatedUser user, String tenantId) {
        requireTenantAccess(user, tenantId, "reporting.read", "platform.tenants.read");
    }
}
