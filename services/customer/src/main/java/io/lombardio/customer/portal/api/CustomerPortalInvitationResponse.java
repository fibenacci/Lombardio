package io.lombardio.customer.portal.api;

public record CustomerPortalInvitationResponse(
        String customerDisplayName,
        String email,
        String status
) {
}
