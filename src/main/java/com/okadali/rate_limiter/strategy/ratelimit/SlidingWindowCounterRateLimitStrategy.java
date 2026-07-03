package com.okadali.rate_limiter.strategy.ratelimit;

import com.okadali.rate_limiter.strategy.intfs.RateLimitStrategy;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "spring.application.rate-limiting-strategy", havingValue = "sliding-window-counter")
public class SlidingWindowCounterRateLimitStrategy implements RateLimitStrategy {
    @Override
    public boolean tryAcquire(HttpServletRequest request) {
        return false;
    }
}
