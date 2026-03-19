package io.lombardio.customer.portal.infrastructure.security;

public record AuthenticatedCustomerPortalUser(
        String customerId,
        String tenantId,
        String displayName,
        String email
) {
}
