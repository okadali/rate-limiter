package com.okadali.rate_limiter.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "spring.rate-limiting")
public class RateLimiterProperties {

    private Configs configs = new Configs();

    @Getter
    @Setter
    public static class Configs {
        private FixedWindow fixedWindow = new FixedWindow();
        private LeakingBucket leakingBucket = new LeakingBucket();
        private SlidingWindowCounter slidingWindowCounter = new SlidingWindowCounter();
        private SlidingWindowLog slidingWindowLog = new SlidingWindowLog();
        private TokenBucket tokenBucket = new TokenBucket();
    }

    @Getter
    @Setter
    public static class FixedWindow {
        private int windowSize;
        private int limit;
    }

    @Getter
    @Setter
    public static class LeakingBucket {
        private int capacity;
        private double leakRate;
    }

    @Getter
    @Setter
    public static class SlidingWindowCounter {
        private int limit;
        private int windowSize;
    }

    @Getter
    @Setter
    public static class SlidingWindowLog {
        private int limit;
        private int windowSize;
    }

    @Getter
    @Setter
    public static class TokenBucket {
        private int capacity;
        private int refillRate;
    }
}
