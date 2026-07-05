package com.okadali.rate_limiter.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.TOO_MANY_REQUESTS, reason = "Rate limit exceeded")
public class RateLimitException extends RuntimeException {


    public RateLimitException() {
    }

    public RateLimitException(String message) {
        super(message);
    }
}
