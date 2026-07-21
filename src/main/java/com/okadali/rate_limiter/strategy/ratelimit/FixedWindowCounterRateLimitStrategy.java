package com.okadali.rate_limiter.strategy.ratelimit;

import com.okadali.rate_limiter.strategy.intfs.RateLimitStrategy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "spring.application.rate-limiting-strategy", havingValue = "fixed-window")
public class FixedWindowCounterRateLimitStrategy implements RateLimitStrategy {

    @Override
    public boolean tryAcquire(ServerHttpRequest request) {
        return false;
    }
}
