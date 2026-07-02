package com.okadali.rate_limiter.controller;

import com.okadali.rate_limiter.service.intfs.RateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
// final fieldlar için constructor oluşturur
@RequiredArgsConstructor
public class GatewayController {

    private final RateLimiterService rateLimiterService;


    @RequestMapping("/**")
    public ResponseEntity<Object> gatewayController(HttpServletRequest request) {
        return new ResponseEntity<>(rateLimiterService.handleRequest(request), HttpStatus.OK);
    }
}
