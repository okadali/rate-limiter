package com.okadali.rate_limiter.strategy.ratelimit;

import com.okadali.rate_limiter.exception.RateLimitException;
import com.okadali.rate_limiter.service.intfs.ReactiveCacheService;
import com.okadali.rate_limiter.strategy.intfs.RateLimitStrategy;
import com.okadali.rate_limiter.util.DataExtractionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

@Component
@ConditionalOnProperty(name = "spring.application.rate-limiting-strategy", havingValue = "fixed-window")
public class FixedWindowCounterRateLimitStrategy implements RateLimitStrategy {

    private final ReactiveStringRedisTemplate redisTemplate;
    private final DefaultRedisScript<Long> fixedWindowScript;

    private final int WINDOW_SIZE_SECONDS = 60;
    private final int LIMIT = 5;

    public FixedWindowCounterRateLimitStrategy(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;

        this.fixedWindowScript = new DefaultRedisScript<>();
        this.fixedWindowScript.setLocation(new ClassPathResource("scripts/lua/fixed_window.lua"));
        this.fixedWindowScript.setResultType(Long.class);
    }

    @Override
    public Mono<Boolean> tryAcquire(ServerHttpRequest request) {
        final String userIp = DataExtractionUtils.extractIpFromRequest(request);

        String key = "rate_limit:fixed_window:" + userIp;

        return redisTemplate.execute(
                fixedWindowScript,
                List.of(key), // Sadece 1 key gönderiyoruz
                List.of(
                        String.valueOf(LIMIT),
                        String.valueOf(WINDOW_SIZE_SECONDS)
                )
        ).next().flatMap(result -> {
            if (result != 1L) {
                return Mono.error(new RateLimitException());
            }
            return Mono.just(true);
        });
    }
}
