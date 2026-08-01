package com.okadali.rate_limiter.strategy.intfs;

import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Mono;

public interface RateLimitStrategy {

    default String getStrategyName() {
        return this.getClass().getSimpleName();
    }

    Mono<Boolean> tryAcquire(ServerHttpRequest request);
}
