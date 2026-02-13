package com.restaurant.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory rate limit: max requests per minute per client key (IP or X-Forwarded-For).
 * Uses a fixed window. For production consider Bucket4j token bucket or Redis.
 */
@Component
@Order(1)
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    @Value("${app.rate-limit.enabled:true}")
    private boolean enabled;

    @Value("${app.rate-limit.requests-per-minute:120}")
    private int requestsPerMinute;

    private final Map<String, Window> windows = new ConcurrentHashMap<>();
    private static final long WINDOW_MS = 60_000;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!enabled) {
            filterChain.doFilter(request, response);
            return;
        }
        String key = clientKey(request);
        long now = System.currentTimeMillis();
        Window w = windows.compute(key, (k, v) -> {
            if (v == null || now - v.startMs > WINDOW_MS)
                return new Window(now, 1);
            return new Window(v.startMs, v.count + 1);
        });
        if (w.count > requestsPerMinute) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Too many requests. Please try again later.\"}");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private static String clientKey(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "unknown";
    }

    private record Window(long startMs, int count) {}
}
