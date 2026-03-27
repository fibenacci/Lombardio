package io.lombardio.identity.portal.api;

public record CustomerPortalMeResponse(
        String customerId,
        String tenantId,
        String displayName,
        String email,
        String onlineAccessStatus
) {
}
