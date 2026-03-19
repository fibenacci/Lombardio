package io.lombardio.identityaccess.ratelimit;

public record RateLimitDecision(
        boolean allowed,
        long retryAfterSeconds
) {
}
