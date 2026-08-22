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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GatewayControllerTest {

    @InjectMocks
    GatewayController gatewayController;

    @Mock
    RateLimiterService rateLimiterService;

    private final DefaultDataBufferFactory bufferFactory = new DefaultDataBufferFactory();
    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        this.webTestClient = WebTestClient.bindToController(gatewayController).build();
    }

    @Test
    void givenAnyRequest_whenControllerInvoked_thenPassthroughToRateLimiter() {
        DataBuffer mockBuffer = bufferFactory.wrap("Success".getBytes());
        Flux<DataBuffer> mockFlux = Flux.just(mockBuffer);
        ResponseEntity<Flux<DataBuffer>> mockResponseEntity = new ResponseEntity<>(mockFlux, HttpStatus.OK);

        when(rateLimiterService.handleRequest(any(ServerHttpRequest.class)))
                .thenReturn(Mono.just(mockResponseEntity));

        webTestClient.get()
                .uri("/gateway")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("Success");
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