package com.okadali.rate_limiter.strategy.ratelimit;

import com.okadali.rate_limiter.exception.RateLimitException;
import com.okadali.rate_limiter.service.intfs.CacheService;
import com.okadali.rate_limiter.strategy.intfs.RateLimitStrategy;
import com.okadali.rate_limiter.util.DataExtractionUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnProperty(
        name = "spring.application.rate-limiting-strategy",
        havingValue = "token-bucket",
        matchIfMissing = true
)
@RequiredArgsConstructor
public class TokenBucketRateLimitStrategy implements RateLimitStrategy {

    private final CacheService cacheService;

    private final int TOKEN_CAPACITY = 4;
    private final long TOKEN_REFILL_PERIOD_IN_SECOND = 60;

    @Override
    public boolean tryAcquire(HttpServletRequest request) {
        final String userIp = DataExtractionUtils.extractIpFromRequest(request);

        if(cacheService.hasKey(userIp)) {
            int accessCount = (int) cacheService.get(userIp);

            if(accessCount < 1) {
                throw new RateLimitException();
            }

            long expiryTime = cacheService.getExpireTime(userIp, TimeUnit.SECONDS);

            cacheService.put(userIp, --accessCount, Duration.ofSeconds(expiryTime));
        }
        else {
            cacheService.put(userIp, TOKEN_CAPACITY - 1, Duration.ofSeconds(TOKEN_REFILL_PERIOD_IN_SECOND));
        }

        return true;
    }
}
