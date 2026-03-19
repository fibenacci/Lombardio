package io.lombardio.identityaccess.ratelimit;

import java.time.Duration;

public interface RateLimitStore {

    RateLimitDecision increment(String key, int maxAttempts, Duration window);
}
