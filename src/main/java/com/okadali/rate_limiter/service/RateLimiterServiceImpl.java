package com.okadali.rate_limiter.service;

import com.okadali.rate_limiter.service.intfs.RateLimiterService;
import com.okadali.rate_limiter.strategy.intfs.RateLimitStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.core.io.buffer.DataBuffer;import java.time.Duration;


@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimiterServiceImpl implements RateLimiterService {

    private final RateLimitStrategy rateLimitStrategy;
    private final WebClient webClient;

    @Override
    public Mono<ResponseEntity<Flux<DataBuffer>>> handleRequest(ServerHttpRequest request) {

        // TODO: rateLimitStrategy.tryAcquire(request);

        // 1. İSTEMCİDEN (Postman) GELEN başlıkları temizle
        HttpHeaders filteredRequestHeaders = new HttpHeaders();
        request.getHeaders().forEach((key, values) -> {
            if (!key.equalsIgnoreCase(HttpHeaders.HOST) &&
                    !key.equalsIgnoreCase(HttpHeaders.CONNECTION) &&
                    !key.equalsIgnoreCase(HttpHeaders.CONTENT_LENGTH) && // WebClient kendi hesaplayacak
                    !key.equalsIgnoreCase(HttpHeaders.TRANSFER_ENCODING) &&
                    !key.equalsIgnoreCase(HttpHeaders.ACCEPT_ENCODING)) { // GZIP çakışmasını önler
                filteredRequestHeaders.addAll(key, values);
            }
        });

        HttpMethod method = request.getMethod();
        WebClient.RequestBodySpec bodySpec = webClient.method(method)
                .uri(uriBuilder -> {
                    uriBuilder.path(request.getURI().getPath());
                    if (request.getURI().getQuery() != null) {
                        uriBuilder.query(request.getURI().getQuery());
                    }
                    return uriBuilder.build();
                })
                .headers(headers -> headers.putAll(filteredRequestHeaders));

        // 2. KRİTİK NOKTA: GET veya HEAD isteklerinde ASLA body ekleme!
        WebClient.RequestHeadersSpec<?> headersSpec;
        if (HttpMethod.GET.equals(method) || HttpMethod.HEAD.equals(method)) {
            headersSpec = bodySpec;
        } else {
            headersSpec = bodySpec.body(BodyInserters.fromDataBuffers(request.getBody()));
        }

        // 3. İsteği gönder ve yanıtı işle
        return headersSpec.exchangeToMono(clientResponse -> {

                    // 4. HEDEFTEN GELEN yanıt başlıklarını temizle
                    HttpHeaders responseHeaders = new HttpHeaders();
                    clientResponse.headers().asHttpHeaders().forEach((key, values) -> {
                        if (!key.equalsIgnoreCase(HttpHeaders.CONTENT_LENGTH) &&
                                !key.equalsIgnoreCase(HttpHeaders.TRANSFER_ENCODING) &&
                                !key.equalsIgnoreCase(HttpHeaders.CONNECTION) &&
                                !key.equalsIgnoreCase(HttpHeaders.CONTENT_ENCODING)) {
                            responseHeaders.addAll(key, values);
                        }
                    });

                    return Mono.just(ResponseEntity.status(clientResponse.statusCode())
                            .headers(responseHeaders)
                            // 5. Akış (stream) sırasında bir kopma olursa logla
                            .body(clientResponse.bodyToFlux(DataBuffer.class)
                                    .doOnError(e -> log.error("Stream transfer sırasında hata: {}", e.getMessage()))));
                })
                .timeout(Duration.ofSeconds(10))
                .doOnError(e -> log.error("Proxy hedef sunucuya bağlanamadı: {}", e.getMessage()))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.BAD_GATEWAY).build()));
    }
}
