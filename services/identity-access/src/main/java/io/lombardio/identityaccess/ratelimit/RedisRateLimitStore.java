package io.lombardio.identityaccess.ratelimit;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnProperty(name = "rate-limit.backend", havingValue = "redis")
public class RedisRateLimitStore implements RateLimitStore {

    private final StringRedisTemplate redisTemplate;

    public RedisRateLimitStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public RateLimitDecision increment(String key, int maxAttempts, Duration window) {
        Long count = redisTemplate.opsForValue().increment(key);
        if (count == null) {
            throw new IllegalStateException("Failed to increment rate limit counter");
        }

        if (count == 1L) {
            redisTemplate.expire(key, window);
        }

        long retryAfterSeconds = 0;
        if (count > maxAttempts) {
            Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            retryAfterSeconds = ttl == null || ttl < 1 ? 1 : ttl;
        }

        return new RateLimitDecision(count <= maxAttempts, retryAfterSeconds);
    }
}
