package com.okadali.rate_limiter.strategy.ratelimit;

import com.okadali.rate_limiter.service.intfs.CacheService;
import com.okadali.rate_limiter.strategy.intfs.RateLimitStrategy;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConditionalOnProperty(
        name = "spring.application.rate-limiting-strategy",
        havingValue = "token-bucket",
        matchIfMissing = true
)
@RequiredArgsConstructor
public class TokenBucketRateLimitStrategy implements RateLimitStrategy {

    private final CacheService cacheService;

    private final int TOKEN_CAPACITY = 10;
    private final long TOKEN_REFILL_PERIOD_IN_SECOND = 10;

    @Override
    public boolean tryAcquire(HttpServletRequest request) {
        String userIp = request.getHeader("X-Forwarded-For");

        if(cacheService.hasKey(userIp)) {
            cacheService.get(userIp);

        }
        else {
            cacheService.put(userIp, TOKEN_CAPACITY - 1, Duration.ofSeconds(TOKEN_REFILL_PERIOD_IN_SECOND));
        }

        return true;
    }



}
