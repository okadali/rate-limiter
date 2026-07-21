package com.okadali.rate_limiter.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import reactor.core.publisher.Mono;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RateLimitException.class)
    public Mono<ResponseEntity<String>> handleRateLimitException(RateLimitException e) {
        // Yanıtı reaktif bir Mono içerisine sararak dönüyoruz
        return Mono.just(
                ResponseEntity
                        .status(HttpStatus.TOO_MANY_REQUESTS) // HTTP 429
                        .body(e.getMessage())
        );
    }

}
