package com.okadali.rate_limiter.util;

import org.springframework.http.server.reactive.ServerHttpRequest;

import java.net.InetSocketAddress;

public final class DataExtractionUtils {
    public static String extractIpFromRequest(ServerHttpRequest request) {
        // 1. NPE yemeden header'ı güvenlice çekiyoruz (getFirst metodu null check yapar)
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");

        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            // 2. Virgülle ayrılmış bir liste geldiyse, her zaman ilk IP asıl istemciye aittir
            return xForwardedFor.split(",")[0].trim();
        }

        // 3. Fallback: Proxy olmadan doğrudan gelindiyse bağlantı adresini al
        InetSocketAddress remoteAddress = request.getRemoteAddress();
        if (remoteAddress != null && remoteAddress.getAddress() != null) {
            return remoteAddress.getAddress().getHostAddress();
        }

        // 4. IP'nin hiçbir şekilde bulunamadığı edge-case senaryosu
        return "unknown";
    }
}
