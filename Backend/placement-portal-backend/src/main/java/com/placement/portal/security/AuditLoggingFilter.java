package com.placement.portal.security;

import com.placement.portal.domain.AuditLog;
import com.placement.portal.repository.AuditLogRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * Servlet filter that asynchronously persists an {@link AuditLog} entry for every
 * mutating HTTP request (POST, PUT, PATCH, DELETE).
 *
 * <p>The log entry is written <em>after</em> the response has been committed, in a
 * fire-and-forget {@link Thread} so that audit I/O never blocks the response path.</p>
 *
 * <h3>Skipped requests</h3>
 * <ul>
 *   <li>OPTIONS pre-flight requests (CORS)</li>
 *   <li>Paths that look like static resources (e.g. {@code .css}, {@code .js},
 *       {@code /actuator/**})</li>
 *   <li>All GET and HEAD requests</li>
 * </ul>
 *
 * <h3>Captured fields</h3>
 * <ul>
 *   <li>{@code userId}    — from {@link SecurityContextHolder}; {@code null} for anonymous requests</li>
 *   <li>{@code action}    — {@code "METHOD /path"}, e.g. {@code "POST /api/auth/login"}</li>
 *   <li>{@code ipAddress} — {@code X-Forwarded-For} header, falling back to remote address</li>
 *   <li>{@code userAgent} — {@code User-Agent} request header</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditLoggingFilter extends OncePerRequestFilter {

    /** HTTP methods whose requests should be audited. */
    private static final Set<String> MUTATING_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");

    /** Path prefixes that are ignored (static assets, actuator). */
    private static final Set<String> SKIP_PREFIXES = Set.of(
            "/actuator", "/favicon.ico", "/static", "/css", "/js", "/images"
    );

    private final AuditLogRepository auditLogRepository;

    // ---------------------------------------------------------------------------
    // Filter implementation
    // ---------------------------------------------------------------------------

    @Override
    protected void doFilterInternal(
            HttpServletRequest  request,
            HttpServletResponse response,
            FilterChain         filterChain
    ) throws ServletException, IOException {
        // Let the request proceed to the next filter / controller
        filterChain.doFilter(request, response);

        // Only audit mutating methods
        String method = request.getMethod();
        if (!MUTATING_METHODS.contains(method)) {
            return;
        }

        String path = request.getRequestURI();

        // Skip static / non-API paths
        if (shouldSkip(path)) {
            return;
        }

        // Capture the security context BEFORE handing off to the async thread
        String userId    = extractUserId();
        String action    = method + " " + path;
        String ipAddress = extractIpAddress(request);
        String userAgent = request.getHeader("User-Agent");

        // Fire-and-forget persistence — never block the response
        Thread.ofVirtual().start(() -> persistAuditLog(userId, action, ipAddress, userAgent));
    }

    // ---------------------------------------------------------------------------
    // Skip predicate
    // ---------------------------------------------------------------------------

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // OPTIONS pre-flight requests are never audited
        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    // ---------------------------------------------------------------------------
    // Private helpers
    // ---------------------------------------------------------------------------

    private boolean shouldSkip(String path) {
        for (String prefix : SKIP_PREFIXES) {
            if (path.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private String extractUserId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()
                    || "anonymousUser".equals(auth.getPrincipal())) {
                return null;
            }
            return auth.getName();
        } catch (Exception e) {
            log.debug("Could not extract userId for audit log", e);
            return null;
        }
    }

    private String extractIpAddress(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            // X-Forwarded-For may contain a comma-separated list; first element is client IP
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void persistAuditLog(String userId, String action, String ipAddress, String userAgent) {
        try {
            AuditLog entry = new AuditLog();
            entry.setUserId(userId);
            entry.setAction(action);
            entry.setIpAddress(ipAddress != null && ipAddress.length() > 45
                    ? ipAddress.substring(0, 45) : ipAddress);
            entry.setUserAgent(userAgent != null && userAgent.length() > 500
                    ? userAgent.substring(0, 500) : userAgent);
            auditLogRepository.save(entry);
        } catch (Exception e) {
            // Audit logging must never propagate exceptions back to the caller
            log.warn("Failed to persist audit log entry: action={} userId={} error={}",
                    action, userId, e.getMessage());
        }
    }
}
