package com.placement.portal.security;

import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple sliding-window rate limiter implemented as a servlet filter.
 *
 * <ul>
 *   <li>{@code POST /api/auth/login}: 20 requests per minute per client IP.</li>
 *   <li>All other paths: 100 requests per minute per authenticated user id,
 *       or per client IP when unauthenticated.</li>
 * </ul>
 *
 * <p>When the limit is exceeded the filter short-circuits the chain and returns
 * HTTP 429 with a JSON body.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final String LOGIN_PATH = "/api/auth/login";
    private static final int LOGIN_LIMIT = 20;
    private static final int GENERAL_LIMIT = 100;
    private static final long WINDOW_MS = 60_000L;

    // Separate buckets for login path and general paths to avoid collisions.
    private final ConcurrentHashMap<String, AtomicInteger> loginCounters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> loginWindowStart = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, AtomicInteger> generalCounters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> generalWindowStart = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Skip rate limiting for CORS preflight requests
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        boolean isLoginPath = LOGIN_PATH.equals(path);

        String key = isLoginPath
                ? getClientIp(request)
                : resolveGeneralKey(request);

        int limit = isLoginPath ? LOGIN_LIMIT : GENERAL_LIMIT;

        ConcurrentHashMap<String, AtomicInteger> counters = isLoginPath ? loginCounters : generalCounters;
        ConcurrentHashMap<String, Long> windowStarts = isLoginPath ? loginWindowStart : generalWindowStart;

        if (isRateLimited(key, limit, counters, windowStarts)) {
            writeTooManyRequestsResponse(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    /**
     * Increments the counter for {@code key} within the current 60-second window.
     *
     * @return {@code true} if the request should be rejected (limit exceeded)
     */
    private boolean isRateLimited(
            String key,
            int limit,
            ConcurrentHashMap<String, AtomicInteger> counters,
            ConcurrentHashMap<String, Long> windowStarts
    ) {
        long now = System.currentTimeMillis();

        // Atomically initialise or reset the window.
        windowStarts.compute(key, (k, existingStart) -> {
            if (existingStart == null || now > existingStart + WINDOW_MS) {
                counters.put(k, new AtomicInteger(0));
                return now;
            }
            return existingStart;
        });

        AtomicInteger counter = counters.computeIfAbsent(key, k -> new AtomicInteger(0));
        int count = counter.incrementAndGet();

        if (count > limit) {
            log.warn("Rate limit exceeded for key='{}' (count={}, limit={})", key, count, limit);
            return true;
        }
        return false;
    }

    /**
     * Returns the key for general (non-login) rate limiting.
     * Uses the authenticated user id when available, otherwise falls back to client IP.
     */
    private String resolveGeneralKey(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            return "user:" + authentication.getName();
        }
        return "ip:" + getClientIp(request);
    }

    /**
     * Resolves the real client IP, respecting the {@code X-Forwarded-For} proxy header.
     */
    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            // Take the first (original client) IP from the comma-separated list.
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void writeTooManyRequestsResponse(HttpServletResponse response) throws IOException {
        response.setStatus(429);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Map<String, Object> body = Map.of("error", "Too Many Requests", "status", 429);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
