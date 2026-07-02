package com.okadali.rate_limiter.strategy.ratelimit;

import com.okadali.rate_limiter.redis.RateLimitResult;
import com.okadali.rate_limiter.strategy.intfs.RateLimitStrategy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        name = "spring.application.rate-limiting-strategy",
        havingValue = "token-bucket",
        matchIfMissing = true
)
public class TokenBucketRateLimitStrategy implements RateLimitStrategy {

    public final int TOKEN_CAPACITY = 10;
    public final int TOKEN_REFILL_COUNT = 4;
    public final long TOKEN_REFILL_PERIOD_IN_SECOND = 10;
}
