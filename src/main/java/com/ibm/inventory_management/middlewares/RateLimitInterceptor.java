package com.ibm.inventory_management.middlewares;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;

public class RateLimitInterceptor implements HandlerInterceptor {

    private final Bucket rLimitBucket;

    public RateLimitInterceptor() {
        Bandwidth limit = Bandwidth.classic(10, Refill.greedy(10, Duration.ofSeconds(1)));
        this.rLimitBucket = Bucket.builder()
                .addLimit(limit)
                .build();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(rLimitBucket.tryConsume(1))
            return true;

        System.out.println("Rate Limit exceeded, request denied.");
        response.setStatus(429);
        return false;
    }

}
