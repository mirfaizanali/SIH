package com.placement.portal.controller;

import com.placement.portal.dto.response.ApiResponse;
import com.placement.portal.service.analytics.AnalyticsService;
import com.placement.portal.service.analytics.AnalyticsService.DashboardStats;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints for platform analytics.
 *
 * <ul>
 *   <li>{@code GET  /api/analytics/dashboard} — aggregated stats for the officer/admin dashboard</li>
 *   <li>{@code POST /api/analytics/refresh}   — evicts the analytics cache (admin only)</li>
 * </ul>
 */
@Slf4j
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    // ---------------------------------------------------------------------------
    // Dashboard
    // ---------------------------------------------------------------------------

    /**
     * Returns aggregated placement statistics.
     *
     * <p>Accessible to placement officers and admins. Results are served from
     * the {@code analytics_dashboard} Caffeine cache (15-minute TTL).</p>
     *
     * @return wrapped {@link DashboardStats}
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('PLACEMENT_OFFICER', 'ADMIN')")
    public ResponseEntity<ApiResponse<DashboardStats>> getDashboard() {
        DashboardStats stats = analyticsService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success("Dashboard statistics retrieved", stats));
    }

    // ---------------------------------------------------------------------------
    // Cache refresh
    // ---------------------------------------------------------------------------

    /**
     * Evicts the analytics dashboard cache, forcing the next {@code GET /dashboard}
     * request to recompute from the database.
     *
     * <p>Admin-only endpoint.</p>
     *
     * @return success confirmation
     */
    @PostMapping("/refresh")
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(value = "analytics_dashboard", allEntries = true)
    public ResponseEntity<ApiResponse<Void>> refreshDashboardCache() {
        log.info("Analytics dashboard cache evicted by admin request");
        return ResponseEntity.ok(ApiResponse.success("Analytics cache refreshed successfully", null));
    }
}
