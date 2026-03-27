package io.lombardio.identity.portal.api;

public record CustomerPortalInvitationResponse(
        String customerDisplayName,
        String email,
        String status
) {
}
