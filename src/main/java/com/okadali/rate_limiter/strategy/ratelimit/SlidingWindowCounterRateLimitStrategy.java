package com.okadali.rate_limiter.strategy.ratelimit;

import com.okadali.rate_limiter.exception.RateLimitException;
import com.okadali.rate_limiter.strategy.intfs.RateLimitStrategy;
import com.okadali.rate_limiter.util.DataExtractionUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@ConditionalOnProperty(name = "spring.application.rate-limiting-strategy", havingValue = "sliding-window-counter")
public class SlidingWindowCounterRateLimitStrategy implements RateLimitStrategy {

    private final ReactiveStringRedisTemplate redisTemplate;
    private final DefaultRedisScript<Long> slidingBucketCounterScript;

    public SlidingWindowCounterRateLimitStrategy(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;

        this.slidingBucketCounterScript = new DefaultRedisScript<>();
        this.slidingBucketCounterScript.setLocation(new ClassPathResource("scripts/lua/sliding_window_counter.lua"));
        this.slidingBucketCounterScript.setResultType(Long.class);
    }

    private final int LIMIT = 10;
    private final int WINDOW_SIZE_SECONDS = 60;

    @Override
    public Mono<Boolean> tryAcquire(ServerHttpRequest request) {
        final String userIp = DataExtractionUtils.extractIpFromRequest(request);
        String baseKey = "rate_limit:sliding_counter:" + userIp;

        long currentTimeMs = System.currentTimeMillis();

        return redisTemplate.execute(
                slidingBucketCounterScript,
                List.of(baseKey),
                List.of(
                        String.valueOf(LIMIT),
                        String.valueOf(WINDOW_SIZE_SECONDS),
                        String.valueOf(currentTimeMs)
                )
        ).next().flatMap(result -> {
            if (result != 1L) {
                return Mono.error(new RateLimitException());
            }
            return Mono.just(true);
        });
    }
}
