package com.okadali.rate_limiter.strategy.ratelimit;

import com.okadali.rate_limiter.exception.RateLimitException;
import com.okadali.rate_limiter.strategy.intfs.RateLimitStrategy;
import com.okadali.rate_limiter.util.DataExtractionUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@ConditionalOnProperty(
        name = "spring.application.rate-limiting-strategy",
        havingValue = "token-bucket",
        matchIfMissing = true
)
public class TokenBucketRateLimitStrategy implements RateLimitStrategy {

    private final ReactiveStringRedisTemplate redisTemplate;
    private final DefaultRedisScript<Long> tokenBucketScript;

    public TokenBucketRateLimitStrategy(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;

        this.tokenBucketScript = new DefaultRedisScript<>();
        this.tokenBucketScript.setLocation(new ClassPathResource("scripts/lua/token_bucket.lua"));
        this.tokenBucketScript.setResultType(Long.class);
    }

    private final int BUCKET_CAPACITY = 1;
    private final long REFILL_RATE = 1;

    @Override
    public Mono<Boolean> tryAcquire(ServerHttpRequest request) {
        final String userIp = DataExtractionUtils.extractIpFromRequest(request);

        String keyCount = "rate_limit:token_bucket:" + userIp + ":count";
        String keyLastRefill = "rate_limit:token_bucket:" + userIp + ":lastRefill";

        List<String> keys = List.of(keyCount, keyLastRefill);
        long currentTimeMs = System.currentTimeMillis();

        return redisTemplate.execute(
                tokenBucketScript,
                keys,
                List.of(
                        String.valueOf(BUCKET_CAPACITY),
                        String.valueOf(REFILL_RATE),
                        String.valueOf(currentTimeMs)
                )
        ).next().flatMap(result -> {
            if(result != 1L) return Mono.error(new RateLimitException());
            return Mono.just(true);
        });
    }
}
