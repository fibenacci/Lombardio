package io.lombardio.identityaccess.ratelimit;

import io.lombardio.identityaccess.config.RateLimitProperties;
import org.springframework.stereotype.Service;

@Service
public class RateLimitService {

    private final RateLimitStore rateLimitStore;
    private final RateLimitProperties properties;

    public RateLimitService(RateLimitStore rateLimitStore, RateLimitProperties properties) {
        this.rateLimitStore = rateLimitStore;
        this.properties = properties;
    }

    public void enforceLoginLimit(String clientKey) {
        enforce("login", clientKey, properties.policies().login());
    }

    public void enforceTotpVerifyLimit(String clientKey) {
        enforce("totp-verify", clientKey, properties.policies().totpVerify());
    }

    public void enforceDelegationLimit(String clientKey) {
        enforce("delegations", clientKey, properties.policies().delegations());
    }

    private void enforce(String scope, String clientKey, RateLimitProperties.Policy policy) {
        RateLimitDecision decision = rateLimitStore.increment(
                "ratelimit:%s:%s".formatted(scope, clientKey),
                policy.maxAttempts(),
                policy.window()
        );

        if (!decision.allowed()) {
            throw new RateLimitExceededException(
                    "Too many requests for " + scope + ". Retry later.",
                    decision.retryAfterSeconds()
            );
        }
    }
}
