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
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "spring.application.rate-limiting-strategy", havingValue = "sliding-window-log")
public class SlidingWindowLogRateLimitStrategy implements RateLimitStrategy {

    private final ReactiveStringRedisTemplate redisTemplate;
    private final DefaultRedisScript<Long> tokenBucketScript;

    public SlidingWindowLogRateLimitStrategy(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;

        this.tokenBucketScript = new DefaultRedisScript<>();
        this.tokenBucketScript.setLocation(new ClassPathResource("scripts/lua/sliding_window_log.lua"));
        this.tokenBucketScript.setResultType(Long.class);
    }

    private final int LIMIT = 10;
    private final long WINDOW_SIZE_SECONDS = 60;

    @Override
    public Mono<Boolean> tryAcquire(ServerHttpRequest request) {
        final String userIp = DataExtractionUtils.extractIpFromRequest(request);
        String key = "rate_limit:sliding_log:" + userIp;

        long currentTimeMs = System.currentTimeMillis();
        // Senin kodundaki harika fikri ZSet'in değer (value) kısmı için kullanıyoruz
        String uniqueRequestId = UUID.randomUUID().toString();

        return redisTemplate.execute(
                tokenBucketScript,
                List.of(key),
                List.of(
                        String.valueOf(LIMIT),
                        String.valueOf(WINDOW_SIZE_SECONDS),
                        String.valueOf(currentTimeMs),
                        uniqueRequestId
                )
        ).next().flatMap(result -> {
            if (result != 1L) {
                return Mono.error(new RateLimitException());
            }
            return Mono.just(true);
        });
    }
}
