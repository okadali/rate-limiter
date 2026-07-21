package com.okadali.rate_limiter.service.intfs;

import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.core.io.buffer.DataBuffer;

public interface RateLimiterService {

    Mono<ResponseEntity<Flux<DataBuffer>>> handleRequest(ServerHttpRequest request);
}
