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
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "spring.application.rate-limiting-strategy", havingValue = "leaking-bucket")
@RequiredArgsConstructor
public class LeakingBucketRateLimitStrategy implements RateLimitStrategy {

    private final ReactiveCacheService reactiveCacheService;

    private final int TOTAL_CAPACITY = 3;
    private final int LEAK_RATE = 60;

    @Override
    public Mono<Boolean> tryAcquire(ServerHttpRequest request) {
        return reactiveCacheService.numberOfTotalKeys()
                .flatMap(totalKeys -> {
                    if(totalKeys >= TOTAL_CAPACITY) {
                        return Mono.error(RateLimitException::new);
                    }

                    return reactiveCacheService.put(UUID.randomUUID().toString(),1, Duration.ofSeconds(LEAK_RATE));

                });
    }
}
