package com.okadali.rate_limiter.util;

import jakarta.servlet.http.HttpServletRequest;

public final class DataExtractionUtils {
    public static String extractIpFromRequest(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if(ip == null) {
            ip = request.getRemoteAddr();
        }

        return ip;
    }
}
