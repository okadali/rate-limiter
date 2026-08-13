package com.okadali.rate_limiter.service;

import com.okadali.rate_limiter.service.intfs.ReactiveCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

//@Service
@RequiredArgsConstructor
@Deprecated
public class ReactiveRedisCacheServiceImpl implements ReactiveCacheService {

    private final ReactiveRedisTemplate<String, Object> redisTemplate;

    @Override
    public Mono<Object> get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public Mono<Boolean> put(String key, Object value, Duration duration) {
        return redisTemplate.opsForValue().set(key, value, duration);
    }

    @Override
    public Mono<Void> reset() {
        return redisTemplate.execute(connection -> connection.serverCommands().flushDb()).then();
    }

    @Override
    public Mono<Boolean> hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    @Override
    public Mono<Duration> getExpireTime(String key) {
        return redisTemplate.getExpire(key);
    }

    @Override
    public Mono<Long> numberOfTotalKeys() {
        return redisTemplate.execute(connection -> connection.serverCommands().dbSize()).next();
    }

    @Override
    public Mono<Long> increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }
}
