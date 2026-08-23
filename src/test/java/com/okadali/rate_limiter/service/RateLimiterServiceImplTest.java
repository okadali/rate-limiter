package com.okadali.rate_limiter.service;

import com.okadali.rate_limiter.exception.RateLimitException;
import com.okadali.rate_limiter.strategy.intfs.RateLimitStrategy;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

//@SpringBootTest for integration tests.
@ExtendWith(MockitoExtension.class)
class RateLimiterServiceImplTest {

    private RateLimiterServiceImpl rateLimiterServiceImpl;

    @Mock
    private RateLimitStrategy rateLimitStrategy;

    private WebClient webClient;
    private MockWebServer mockWebServer;

    @BeforeEach
    void setUp() throws IOException {
        this.mockWebServer = new MockWebServer();
        this.mockWebServer.start();

        this.webClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/products").toString())
                .build();

        this.rateLimiterServiceImpl = new RateLimiterServiceImpl(rateLimitStrategy, webClient);
    }

    @AfterEach
    void tearDown() throws IOException {
        this.mockWebServer.shutdown();
    }

    @Test
    void givenRequest_whenNoLimit_thenThrowException() {
        ServerHttpRequest request = MockServerHttpRequest
                .get("/products")
                .header("HOST", "0.1.2.3")
                .build();

        when(rateLimitStrategy.tryAcquire(any())).thenReturn(Mono.error(new RateLimitException()));

        StepVerifier.create(rateLimiterServiceImpl.handleRequest(request))
                .expectError(RateLimitException.class)
                .verify();
    }

    @Test
    void givenGetRequest_whenHasLimit_thenReturnDataAndRemoveProducts() {
        String expectedContent = "Selam";
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(expectedContent));

        ServerHttpRequest request = MockServerHttpRequest
                .get("/products")
                .header("HOST", "0.1.2.3")
                .build();

        when(rateLimitStrategy.tryAcquire(any())).thenReturn(Mono.just(true));

        Mono<ResponseEntity<Flux<DataBuffer>>> responseMono = rateLimiterServiceImpl.handleRequest(request);

        Mono<String> bodyContentMono = responseMono
                .doOnNext(responseEntity -> {
                    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
                    assertNotNull(responseEntity.getBody());
                })
                .flatMapMany(ResponseEntity::getBody)
                .collectList()
                .flatMap(buffers ->
                        DataBufferUtils.join(Flux.fromIterable(buffers))
                                .map(buffer -> {
                                    String content = buffer.toString(StandardCharsets.UTF_8);
                                    DataBufferUtils.release(buffer);
                                    return content;
                                })
                );

        StepVerifier.create(bodyContentMono)
                .expectNext(expectedContent)
                .verifyComplete();
    }
}