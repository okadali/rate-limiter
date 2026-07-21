package com.okadali.rate_limiter.strategy.intfs;

import org.springframework.http.server.reactive.ServerHttpRequest;

public interface RateLimitStrategy {

    default String getStrategyName() {
        return this.getClass().getSimpleName();
    }

    boolean tryAcquire(ServerHttpRequest request);
}
