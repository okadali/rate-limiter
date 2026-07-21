package com.okadali.rate_limiter.controller;

import com.okadali.rate_limiter.service.intfs.RateLimiterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.core.io.buffer.DataBuffer;
@RestController
// final fieldlar için constructor oluşturur
@RequiredArgsConstructor
public class GatewayController {

    private final RateLimiterService rateLimiterService;

    @RequestMapping("/**")
    public Mono<ResponseEntity<Flux<DataBuffer>>> gatewayController(ServerHttpRequest request) {
        return rateLimiterService.handleRequest(request);
    }
}
