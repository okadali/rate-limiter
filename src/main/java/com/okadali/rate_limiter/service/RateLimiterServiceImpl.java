package com.okadali.rate_limiter.service;

import com.okadali.rate_limiter.redis.RateLimitResult;
import com.okadali.rate_limiter.service.intfs.RateLimiterService;
import com.okadali.rate_limiter.strategy.intfs.RateLimitStrategy;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RateLimiterServiceImpl implements RateLimiterService {


    private final RateLimitStrategy rateLimitStrategy;

    @Override
    public RateLimitResult handleRequest(HttpServletRequest request) {
        return rateLimitStrategy.tryAcquire();
    }
}
