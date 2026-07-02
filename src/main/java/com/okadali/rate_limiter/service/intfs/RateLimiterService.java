package com.okadali.rate_limiter.service.intfs;

import com.okadali.rate_limiter.redis.RateLimitResult;
import jakarta.servlet.http.HttpServletRequest;

public interface RateLimiterService {

    RateLimitResult handleRequest(HttpServletRequest request);
}
