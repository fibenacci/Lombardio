package io.lombardio.identityaccess.auth.domain;

import java.time.Instant;

public record TotpCredential(
        String userId,
        String secretCiphertext,
        boolean enabled,
        Instant createdAt,
        Instant activatedAt
) {
}
