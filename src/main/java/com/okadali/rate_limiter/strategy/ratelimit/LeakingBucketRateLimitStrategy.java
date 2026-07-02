package com.okadali.rate_limiter.strategy.ratelimit;

import com.okadali.rate_limiter.strategy.intfs.RateLimitStrategy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "spring.application.rate-limiting-strategy", havingValue = "leaking-bucket")
public class LeakingBucketRateLimitStrategy implements RateLimitStrategy {
}
