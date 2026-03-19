package io.lombardio.identityaccess.ratelimit;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConditionalOnProperty(name = "rate-limit.backend", havingValue = "memory", matchIfMissing = true)
public class InMemoryRateLimitStore implements RateLimitStore {

    private final ConcurrentHashMap<String, WindowCounter> counters = new ConcurrentHashMap<>();
    private final Clock clock;

    public InMemoryRateLimitStore(Clock clock) {
        this.clock = clock;
    }

    @Override
    public RateLimitDecision increment(String key, int maxAttempts, Duration window) {
        Instant now = Instant.now(clock);
        WindowCounter counter = counters.compute(key, (ignored, current) -> {
            if (current == null || now.isAfter(current.expiresAt())) {
                return new WindowCounter(1, now.plus(window));
            }
            return new WindowCounter(current.count() + 1, current.expiresAt());
        });

        long retryAfterSeconds = 0;
        if (counter.count() > maxAttempts) {
            retryAfterSeconds = Math.max(1, Duration.between(now, counter.expiresAt()).getSeconds());
        }

        return new RateLimitDecision(counter.count() <= maxAttempts, retryAfterSeconds);
    }

    private record WindowCounter(int count, Instant expiresAt) {
    }
}
