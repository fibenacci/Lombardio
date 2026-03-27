package io.lombardio.identity.portal.api;

public record CustomerPortalLoginResponse(
        String accessToken,
        CustomerPortalMeResponse customer
) {
}
