package com.placement.portal.service.notification;

import com.placement.portal.dto.response.NotificationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Thin wrapper around {@link SimpMessagingTemplate} that provides a clean API
 * for pushing STOMP messages to individual users and broadcast topics.
 *
 * <p>User-specific messages are delivered to
 * {@code /user/{userId}/queue/notifications}, which the WebSocket broker
 * resolves using the user-destination prefix {@code /user} configured in
 * {@link com.placement.portal.config.WebSocketConfig}.</p>
 *
 * <p>Broadcast messages are sent directly to the supplied destination (e.g.
 * {@code /topic/drives}) and are consumed by all subscribed clients.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Pushes a notification to a single user's personal queue.
     *
     * <p>The STOMP broker resolves the full destination as
     * {@code /user/{userId}/queue/notifications}.</p>
     *
     * @param userId       the UUID of the target user (used as the STOMP user name)
     * @param notification the notification payload to deliver
     */
    public void sendToUser(String userId, NotificationDto notification) {
        if (userId == null || userId.isBlank()) {
            log.warn("WebSocketNotificationService.sendToUser called with blank userId — skipping");
            return;
        }
        log.debug("Sending WebSocket notification to userId={} type={}", userId, notification.getType());
        messagingTemplate.convertAndSendToUser(userId, "/queue/notifications", notification);
    }

    /**
     * Broadcasts a payload to all clients subscribed to the given destination.
     *
     * <p>Typical use: drive announcements on {@code /topic/drives}.</p>
     *
     * @param destination the full STOMP destination (e.g. {@code /topic/drives})
     * @param payload     the object to serialise and send; must be JSON-serialisable
     */
    public void sendToAll(String destination, Object payload) {
        if (destination == null || destination.isBlank()) {
            log.warn("WebSocketNotificationService.sendToAll called with blank destination — skipping");
            return;
        }
        log.debug("Broadcasting WebSocket message to destination={}", destination);
        messagingTemplate.convertAndSend(destination, payload);
    }
}
