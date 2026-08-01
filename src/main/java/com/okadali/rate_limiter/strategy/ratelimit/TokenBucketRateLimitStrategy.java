package com.okadali.rate_limiter.strategy.ratelimit;

import com.okadali.rate_limiter.exception.RateLimitException;
import com.okadali.rate_limiter.service.intfs.ReactiveCacheService;
import com.okadali.rate_limiter.strategy.intfs.RateLimitStrategy;
import com.okadali.rate_limiter.util.DataExtractionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
@ConditionalOnProperty(
        name = "spring.application.rate-limiting-strategy",
        havingValue = "token-bucket",
        matchIfMissing = true
)
@RequiredArgsConstructor
public class TokenBucketRateLimitStrategy implements RateLimitStrategy {

    private final ReactiveCacheService cacheService;

    private final int TOKEN_CAPACITY = 4;
    private final long TOKEN_REFILL_PERIOD_IN_SECOND = 60;

    @Override
    public Mono<Boolean> tryAcquire(ServerHttpRequest request) {
        final String userIp = DataExtractionUtils.extractIpFromRequest(request);

        return cacheService.get(userIp)
                .flatMap(cachedValue -> {
                    int accessCount = (int) cachedValue;

                    if(accessCount < 1) {
                        return Mono.error(new RateLimitException());
                    }

                    return cacheService.getExpireTime(userIp)
                            .flatMap(expiryDuration ->
                                cacheService.put(userIp, accessCount - 1, expiryDuration)
                            );
                })
                .switchIfEmpty(Mono.defer(() ->
                    cacheService.put(userIp, TOKEN_CAPACITY - 1, Duration.ofSeconds(TOKEN_REFILL_PERIOD_IN_SECOND))
                ));
    }
}
