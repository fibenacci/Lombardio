package io.lombardio.identityaccess.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "rate-limit")
public record RateLimitProperties(
        String backend,
        Policies policies
) {

    public record Policies(
            Policy login,
            Policy totpVerify,
            Policy delegations
    ) {
    }

    public record Policy(
            int maxAttempts,
            Duration window
    ) {
    }
}
