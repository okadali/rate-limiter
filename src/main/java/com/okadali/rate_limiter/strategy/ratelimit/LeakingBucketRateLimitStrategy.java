package com.okadali.rate_limiter.strategy.ratelimit;

import com.okadali.rate_limiter.exception.RateLimitException;
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

import java.util.List;

@Component
@ConditionalOnProperty(name = "spring.application.rate-limiting-strategy", havingValue = "leaking-bucket")
public class LeakingBucketRateLimitStrategy implements RateLimitStrategy {

    private final ReactiveStringRedisTemplate redisTemplate;
    private final DefaultRedisScript<Long> leakingBucketScript;


    public LeakingBucketRateLimitStrategy(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;

        this.leakingBucketScript = new DefaultRedisScript<>();
        this.leakingBucketScript.setLocation(new ClassPathResource("scripts/lua/leaky_bucket.lua"));
        this.leakingBucketScript.setResultType(Long.class);
    }

    private final int BUCKET_CAPACITY = 10;
    private final double LEAK_RATE = 1.0;

    @Override
    public Mono<Boolean> tryAcquire(ServerHttpRequest request) {
        final String userIp = DataExtractionUtils.extractIpFromRequest(request);

        String waterKey = "rate_limit:leaky_bucket:water:" + userIp;
        String timeKey = "rate_limit:leaky_bucket:time:" + userIp;

        long currentTimeMs = System.currentTimeMillis();

        return redisTemplate.execute(
                leakingBucketScript,
                List.of(waterKey, timeKey),
                List.of(
                        String.valueOf(BUCKET_CAPACITY),
                        String.valueOf(LEAK_RATE),
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
