package com.okadali.rate_limiter.strategy.ratelimit;

import com.okadali.rate_limiter.strategy.intfs.RateLimitStrategy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@ConditionalOnProperty(name = "spring.application.rate-limiting-strategy", havingValue = "sliding-window-counter")
public class SlidingWindowCounterRateLimitStrategy implements RateLimitStrategy {
    @Override
    public Mono<Boolean> tryAcquire(ServerHttpRequest request) {
        return Mono.just(true);
    }
}
