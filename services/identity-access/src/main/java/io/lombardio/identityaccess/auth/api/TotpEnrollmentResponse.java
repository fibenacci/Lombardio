package io.lombardio.identityaccess.auth.api;

public record TotpEnrollmentResponse(
        String secret,
        String otpauthUri,
        boolean enabled
) {
}
