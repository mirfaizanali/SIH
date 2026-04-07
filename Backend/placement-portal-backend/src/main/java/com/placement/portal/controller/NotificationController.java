package com.placement.portal.controller;

import com.placement.portal.dto.response.ApiResponse;
import com.placement.portal.dto.response.NotificationDto;
import com.placement.portal.dto.response.PagedResponse;
import com.placement.portal.service.notification.NotificationService;
import com.placement.portal.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST API for notification management.
 *
 * <p>All endpoints require the user to be authenticated. User identity is
 * resolved from the JWT via {@link SecurityUtils#getCurrentUserId()}.</p>
 *
 * <table border="1">
 *   <caption>Endpoint summary</caption>
 *   <tr><th>Method</th><th>Path</th><th>Description</th></tr>
 *   <tr><td>GET</td><td>/api/notifications</td>
 *       <td>Paged list of notifications for the current user</td></tr>
 *   <tr><td>PATCH</td><td>/api/notifications/{id}/read</td>
 *       <td>Marks a single notification as read</td></tr>
 *   <tr><td>PATCH</td><td>/api/notifications/read-all</td>
 *       <td>Marks all notifications as read and returns the count updated</td></tr>
 *   <tr><td>GET</td><td>/api/notifications/unread-count</td>
 *       <td>Returns the unread notification count</td></tr>
 * </table>
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final SecurityUtils securityUtils;

    // -----------------------------------------------------------------------
    // GET /api/notifications
    // -----------------------------------------------------------------------

    /**
     * Returns a paged, newest-first list of all notifications for the
     * currently authenticated user.
     *
     * <p>Default page size is 20. Override via {@code ?page=0&size=10}.</p>
     *
     * @param pageable pagination parameters resolved by Spring MVC
     * @return {@code 200 OK} with a {@link PagedResponse} of {@link NotificationDto}
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PagedResponse<NotificationDto>>> getMyNotifications(
            @PageableDefault(size = 20) Pageable pageable) {

        String userId = securityUtils.getCurrentUserId();
        PagedResponse<NotificationDto> response =
                notificationService.getNotificationsForUser(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // -----------------------------------------------------------------------
    // PATCH /api/notifications/{id}/read
    // -----------------------------------------------------------------------

    /**
     * Marks a single notification as read.
     *
     * <p>Ownership is enforced — users may only mark their own notifications.</p>
     *
     * @param id the UUID of the notification to mark read
     * @return {@code 200 OK} with the updated {@link NotificationDto}
     */
    @PatchMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<NotificationDto>> markAsRead(@PathVariable String id) {
        String userId = securityUtils.getCurrentUserId();
        NotificationDto dto = notificationService.markAsRead(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", dto));
    }

    // -----------------------------------------------------------------------
    // PATCH /api/notifications/read-all
    // -----------------------------------------------------------------------

    /**
     * Marks every unread notification of the current user as read.
     *
     * @return {@code 200 OK} with {@code { "updated": N }} where {@code N} is
     *         the count of notifications that were updated
     */
    @PatchMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> markAllRead() {
        String userId  = securityUtils.getCurrentUserId();
        int updated    = notificationService.markAllAsRead(userId);
        Map<String, Integer> body = Map.of("updated", updated);
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read", body));
    }

    // -----------------------------------------------------------------------
    // GET /api/notifications/unread-count
    // -----------------------------------------------------------------------

    /**
     * Returns the number of unread notifications for the current user.
     *
     * @return {@code 200 OK} with {@code { "count": N }}
     */
    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount() {
        String userId = securityUtils.getCurrentUserId();
        long count    = notificationService.getUnreadCount(userId);
        Map<String, Long> body = Map.of("count", count);
        return ResponseEntity.ok(ApiResponse.success(body));
    }
}
