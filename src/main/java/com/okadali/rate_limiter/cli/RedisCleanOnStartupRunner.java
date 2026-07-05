package com.okadali.rate_limiter.cli;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile({"dev"})
public class RedisCleanOnStartupRunner implements CommandLineRunner {

    private final RedisTemplate<String, Object> redisTemplate;


    @Override
    public void run(String... args) {
        redisTemplate.execute((RedisCallback<String>) (connection) -> {
            connection.serverCommands().flushAll();
            return "OK";
        });

        log.info("Redis Sucessfully cleaned up");
    }
}
