package com.okadali.rate_limiter.strategy.intfs;

import com.okadali.rate_limiter.redis.RateLimitResult;

public interface RateLimitStrategy {

    default RateLimitResult tryAcquire() {
        return new RateLimitResult(this.getClass().getSimpleName());
    }
}
