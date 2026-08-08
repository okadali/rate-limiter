package com.okadali.rate_limiter.service.intfs;

import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public interface ReactiveCacheService {
    Mono<Object> get(String key);

    Mono<Boolean> put(String key, Object value, Duration duration);

    Mono<Void> reset();

    Mono<Boolean> hasKey(String key);

    Mono<Duration> getExpireTime(String key);

    Mono<Long> numberOfTotalKeys();
}
