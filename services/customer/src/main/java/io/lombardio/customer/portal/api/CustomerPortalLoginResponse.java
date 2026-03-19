package io.lombardio.customer.portal.api;

public record CustomerPortalLoginResponse(
        String accessToken,
        CustomerPortalMeResponse customer
) {
}
