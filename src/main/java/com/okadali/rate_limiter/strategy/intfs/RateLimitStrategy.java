package com.okadali.rate_limiter.strategy.intfs;

import com.okadali.rate_limiter.redis.RateLimitResult;
import jakarta.servlet.http.HttpServletRequest;

public interface RateLimitStrategy {

    default String getStrategyName() {
        return this.getClass().getSimpleName();
    }

    boolean tryAcquire(HttpServletRequest request);
}
