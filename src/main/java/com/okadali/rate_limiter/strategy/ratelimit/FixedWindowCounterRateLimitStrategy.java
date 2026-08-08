package com.okadali.rate_limiter.strategy.ratelimit;

import com.okadali.rate_limiter.exception.RateLimitException;
import com.okadali.rate_limiter.service.intfs.ReactiveCacheService;
import com.okadali.rate_limiter.strategy.intfs.RateLimitStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

@Component
@ConditionalOnProperty(name = "spring.application.rate-limiting-strategy", havingValue = "fixed-window")
@RequiredArgsConstructor
public class FixedWindowCounterRateLimitStrategy implements RateLimitStrategy {

    private final ReactiveCacheService cacheService;

    private final int TOTAL_CAPACITY_IN_WINDOW = 5;

    @Override
    public Mono<Boolean> tryAcquire(ServerHttpRequest request) {
        String currMinute = String.valueOf(Instant.now().atZone(ZoneOffset.UTC).getMinute());

        return cacheService.get(currMinute)
                .flatMap(cachedValue -> {
                    int accessCount = (int) cachedValue;

                    if (accessCount >= TOTAL_CAPACITY_IN_WINDOW - 1) {
                        return Mono.error(new RateLimitException());
                    }

                    return cacheService.increment(currMinute).then(Mono.just(true));
                })
                .switchIfEmpty(Mono.defer(() ->
                        cacheService.put(currMinute, 0, Duration.ofSeconds(60))
                ));
    }
}
