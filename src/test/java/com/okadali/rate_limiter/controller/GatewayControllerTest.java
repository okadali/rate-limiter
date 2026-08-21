package com.okadali.rate_limiter.controller;

import com.okadali.rate_limiter.service.intfs.RateLimiterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GatewayControllerTest {

    private final DefaultDataBufferFactory bufferFactory = new DefaultDataBufferFactory();


    Mono<ResponseEntity<Flux<DataBuffer>>> successResponse;
    Mono<ResponseEntity<Flux<DataBuffer>>> errorResponse;

    @InjectMocks
    GatewayController gatewayController;

    @Mock
    RateLimiterService rateLimiterService;

    @BeforeEach
    void setUp() {
        successResponse = Mono.just(ResponseEntity.ok().body(Flux.just(bufferFactory.wrap("Success".getBytes()))));
        errorResponse = Mono.just(ResponseEntity.status(429).body(Flux.just(bufferFactory.wrap("Too Many Requests".getBytes()))));
    }

    @Test
    void givenServerHttpRequest_whenPathExists_thenReturnResponse() {
        ServerHttpRequest request = MockServerHttpRequest
                .get("/test/path")
                .build();


        when(rateLimiterService.handleRequest(request)).thenReturn(successResponse);

        Mono<ResponseEntity<Flux<DataBuffer>>> response = gatewayController.gatewayController(request);


    }
}

//        StepVerifier.create(response)
//                .assertNext(responseEntity -> {
//                    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
//
//                    Flux<DataBuffer> body = responseEntity.getBody();
//
//                    StepVerifier.create(body)
//                            .assertNext(dataBuffer -> {
//                                String content = dataBuffer.toString(StandardCharsets.UTF_8);
//                                assertEquals("Success", content);
//                            })
//                            .verifyComplete();
//                })
//                .verifyComplete();