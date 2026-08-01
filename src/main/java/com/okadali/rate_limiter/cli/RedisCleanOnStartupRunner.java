package com.okadali.rate_limiter.cli;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile({"dev"})
public class RedisCleanOnStartupRunner implements CommandLineRunner {

    private final ReactiveRedisTemplate<String, Object> redisTemplate;


    @Override
    public void run(String... args) {

        redisTemplate.execute(connection -> connection.serverCommands().flushAll())
                .then()
                .doOnSuccess(v -> log.info("Redis Successfully cleaned up"))
                .doOnError(e -> log.error("Redis cleanup failed on startup!", e))
                .block();
    }
}
