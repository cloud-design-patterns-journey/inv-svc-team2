package com.ibm.inventory_management.interceptors;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final int MAX_REQUESTS = 5;
    private static final long WINDOW_MS = 60_000;
    private static final Set<String> WRITE_METHODS = Set.of("POST", "PUT", "DELETE");

    private final ConcurrentHashMap<String, AtomicInteger> counts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> windowStarts = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!WRITE_METHODS.contains(request.getMethod())) {
            return true;
        }

        String ip = request.getRemoteAddr();
        long now = System.currentTimeMillis();

        windowStarts.putIfAbsent(ip, now);
        counts.putIfAbsent(ip, new AtomicInteger(0));

        if (now - windowStarts.get(ip) > WINDOW_MS) {
            windowStarts.put(ip, now);
            counts.put(ip, new AtomicInteger(0));
        }

        int count = counts.get(ip).incrementAndGet();
        if (count > MAX_REQUESTS) {
            long retryAfter = (WINDOW_MS - (now - windowStarts.get(ip))) / 1000 + 1;
            response.setStatus(429);
            response.setContentType("application/json");
            response.setHeader("Retry-After", String.valueOf(retryAfter));
            response.getWriter().write(
                "{\"error\": \"Too Many Requests\", \"message\": \"Rate limit exceeded. Max "
                + MAX_REQUESTS + " write requests per minute per IP.\", \"retryAfter\": " + retryAfter + "}"
            );
            return false;
        }

        return true;
    }
}
