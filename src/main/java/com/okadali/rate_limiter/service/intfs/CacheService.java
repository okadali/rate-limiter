package com.okadali.rate_limiter.service.intfs;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public interface CacheService {

    Object get(String key);

    void put(String key, Object value, Duration duration);

    void reset();

    boolean hasKey(String key);

    long getExpireTime(String key, TimeUnit timeUnit);
}
