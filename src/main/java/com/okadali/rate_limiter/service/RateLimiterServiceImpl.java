package com.okadali.rate_limiter.service;

import com.okadali.rate_limiter.service.intfs.RateLimiterService;
import com.okadali.rate_limiter.strategy.intfs.RateLimitStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimiterServiceImpl implements RateLimiterService {

    private final RateLimitStrategy rateLimitStrategy;
    private final WebClient webClient;

    @Override
    public Mono<ResponseEntity<Flux<DataBuffer>>> handleRequest(ServerHttpRequest request) {

        rateLimitStrategy.tryAcquire(request);

        return rateLimitStrategy.tryAcquire(request).flatMap(validAcquire -> {
            return webClient.method(request.getMethod())
                    .uri(request.getURI().getPath())
                    .headers(headers -> {
                        headers.addAll(request.getHeaders());
                        // CRITICAL: Remove original Host header so dummyjson.com doesn't reject it
                        headers.remove(HttpHeaders.HOST);
                    })
                    .body(BodyInserters.fromDataBuffers(request.getBody()))
                    .retrieve()
                    // Converts the response into an asynchronous entity stream
                    .toEntityFlux(DataBuffer.class)
                    // Gracefully handle errors so the connection drops cleanly if something fails
                    .onErrorResume(WebClientResponseException.class, ex -> Mono.just(
                            ResponseEntity.status(ex.getStatusCode())
                                    .headers(ex.getHeaders())
                                    .body(Flux.empty())
                    ));
        });
    }

// DEPRECATED
//    @RequestMapping(value = "/api/**", method = {RequestMethod.GET, RequestMethod.POST})
//    public Mono<ResponseEntity<Flux<DataBuffer>>> proxy(ServerHttpRequest request) {
//
//        return webClient.method(request.getMethod())
//                .uri(request.getURI().getPath())
//                .headers(headers -> headers.addAll(request.getHeaders()))
//                .body(request.getBody(), DataBuffer.class)
//                .exchangeToMono(response -> Mono.just(
//                        ResponseEntity.status(response.statusCode())
//                                .headers(response.headers().asHttpHeaders())
//                                .body(response.bodyToFlux(DataBuffer.class))
//                ));
//    }
}
