package com.okadali.rate_limiter.service.intfs;

import java.awt.desktop.AboutEvent;
import java.time.Duration;

public interface CacheService {

    Object get(String key);

    void put(String key, Object value, Duration duration);

    void reset();

    boolean hasKey(String key);
}
