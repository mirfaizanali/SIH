package com.placement.portal.service.notification;

import com.placement.portal.domain.Notification;
import com.placement.portal.domain.User;
import com.placement.portal.domain.enums.NotificationType;
import com.placement.portal.dto.response.NotificationDto;
import com.placement.portal.dto.response.PagedResponse;
import com.placement.portal.exception.EntityNotFoundException;
import com.placement.portal.mapper.EntityMapper;
import com.placement.portal.repository.NotificationRepository;
import com.placement.portal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Central orchestrator for all in-app and email notifications.
 *
 * <p>Every public method follows a consistent three-step flow:
 * <ol>
 *   <li>Persist a {@link Notification} entity to the database so that users
 *       can see notification history in the UI.</li>
 *   <li>Push the notification to the user's STOMP queue via
 *       {@link WebSocketNotificationService} for instant in-app delivery.</li>
 *   <li>Optionally fire an async email through {@link EmailNotificationService}
 *       (fire-and-forget — never blocks the caller, never throws).</li>
 * </ol>
 *
 * <p>All database writes are transactional. WebSocket and email calls are
 * intentionally made outside the strict transactional boundary where possible
 * (they are side effects and must not cause the DB transaction to roll back on
 * failure).</p>
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {

    private static final DateTimeFormatter INTERVIEW_FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final WebSocketNotificationService webSocketNotificationService;
    private final EmailNotificationService emailNotificationService;
    private final EntityMapper entityMapper;

    // -----------------------------------------------------------------------
    // Convenience notification methods
    // -----------------------------------------------------------------------

    /**
     * Notifies a student that their job/internship application was successfully submitted.
     *
     * @param studentUserId the UUID of the student's {@link com.placement.portal.domain.User}
     * @param applicationId the UUID of the new {@link com.placement.portal.domain.Application}
     * @param jobTitle      the title of the job or internship applied to
     */
    public void notifyApplicationSubmitted(String studentUserId,
                                           String applicationId,
                                           String jobTitle) {
        String title   = "Application Submitted";
        String message = "Your application for \"" + jobTitle + "\" has been submitted successfully. "
                       + "We will notify you of any updates.";
        createAndDeliver(
                studentUserId,
                NotificationType.APPLICATION_STATUS,
                title,
                message,
                "APPLICATION",
                applicationId
        );
    }

    /**
     * Notifies a student that the status of their application has changed
     * (e.g. shortlisted, rejected, offered).
     *
     * @param studentUserId the UUID of the student's {@link com.placement.portal.domain.User}
     * @param applicationId the UUID of the {@link com.placement.portal.domain.Application}
     * @param newStatus     the new status label (e.g. {@code "SHORTLISTED"})
     * @param jobTitle      the title of the job or internship
     */
    public void notifyApplicationStatusChanged(String studentUserId,
                                               String applicationId,
                                               String newStatus,
                                               String jobTitle) {
        String title   = "Application Update";
        String message = "Your application for \"" + jobTitle + "\" has been "
                       + newStatus.toLowerCase() + ".";
        createAndDeliver(
                studentUserId,
                NotificationType.APPLICATION_STATUS,
                title,
                message,
                "APPLICATION",
                applicationId
        );
    }

    /**
     * Notifies both the student and the employer that an interview has been scheduled.
     *
     * @param studentUserId  the UUID of the student's {@link com.placement.portal.domain.User}
     * @param employerUserId the UUID of the employer's {@link com.placement.portal.domain.User}
     * @param interviewId    the UUID of the {@link com.placement.portal.domain.Interview}
     * @param scheduledAt    the date and time of the interview
     * @param jobTitle       the title of the position being interviewed for
     */
    public void notifyInterviewScheduled(String studentUserId,
                                         String employerUserId,
                                         String interviewId,
                                         LocalDateTime scheduledAt,
                                         String jobTitle) {
        String formattedTime = scheduledAt != null
                ? scheduledAt.format(INTERVIEW_FMT) : "TBD";

        // Notification for the student
        String studentTitle   = "Interview Scheduled";
        String studentMessage = "Your interview for \"" + jobTitle + "\" has been scheduled on "
                              + formattedTime + ". Please be prepared!";
        createAndDeliver(
                studentUserId,
                NotificationType.INTERVIEW_SCHEDULED,
                studentTitle,
                studentMessage,
                "INTERVIEW",
                interviewId
        );

        // Notification for the employer
        String employerTitle   = "Interview Scheduled";
        String employerMessage = "An interview for the position \"" + jobTitle
                               + "\" has been scheduled on " + formattedTime + ".";
        createAndDeliver(
                employerUserId,
                NotificationType.INTERVIEW_SCHEDULED,
                employerTitle,
                employerMessage,
                "INTERVIEW",
                interviewId
        );
    }

    /**
     * Notifies a list of students that a new job has been posted.
     *
     * <p>Email is intentionally skipped for this event — it would be too noisy
     * when many students are eligible. Only a WebSocket push is made; the
     * notification is still persisted in the database.</p>
     *
     * @param studentUserIds list of student user UUIDs to notify
     * @param jobId          the UUID of the new {@link com.placement.portal.domain.Job}
     * @param jobTitle       the title of the new job
     * @param companyName    the name of the posting company
     */
    public void notifyNewJobPosted(List<String> studentUserIds,
                                   String jobId,
                                   String jobTitle,
                                   String companyName) {
        if (studentUserIds == null || studentUserIds.isEmpty()) {
            return;
        }
        String title   = "New Job Opportunity";
        String message = companyName + " has posted a new position: \"" + jobTitle
                       + "\". Apply before the deadline!";

        for (String userId : studentUserIds) {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                log.warn("notifyNewJobPosted: user not found for userId={}, skipping", userId);
                continue;
            }
            Notification saved = persistNotification(user, NotificationType.NEW_JOB,
                    title, message, "JOB", jobId);
            NotificationDto dto = entityMapper.toNotificationDto(saved);

            // WebSocket only — no email for new-job broadcasts
            webSocketNotificationService.sendToUser(userId, dto);
        }
    }

    /**
     * Notifies a student that their submitted report has received faculty feedback.
     *
     * @param studentUserId the UUID of the student's {@link com.placement.portal.domain.User}
     * @param reportId      the UUID of the {@link com.placement.portal.domain.Report}
     * @param reportTitle   the title of the report
     * @param status        the review status (e.g. {@code "APPROVED"}, {@code "REJECTED"})
     */
    public void notifyReportFeedback(String studentUserId,
                                     String reportId,
                                     String reportTitle,
                                     String status) {
        String title   = "Report Feedback Received";
        String message = "Your report \"" + reportTitle + "\" has been reviewed and marked as "
                       + status.toLowerCase() + ".";
        createAndDeliver(
                studentUserId,
                NotificationType.REPORT_FEEDBACK,
                title,
                message,
                "REPORT",
                reportId
        );
    }

    /**
     * Notifies a list of students that a placement drive has been announced.
     *
     * <p>Like {@link #notifyNewJobPosted}, email is skipped to avoid inbox
     * flooding. Only WebSocket + DB persistence is performed.</p>
     *
     * @param studentUserIds list of student user UUIDs to notify
     * @param driveId        the UUID of the {@link com.placement.portal.domain.PlacementDrive}
     * @param driveTitle     the title of the drive
     * @param companyName    the name of the company hosting the drive
     */
    public void notifyDriveAnnounced(List<String> studentUserIds,
                                     String driveId,
                                     String driveTitle,
                                     String companyName) {
        if (studentUserIds == null || studentUserIds.isEmpty()) {
            return;
        }
        String title   = "Placement Drive Announced";
        String message = companyName + " is conducting a placement drive: \"" + driveTitle
                       + "\". Check the details and register now!";

        for (String userId : studentUserIds) {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                log.warn("notifyDriveAnnounced: user not found for userId={}, skipping", userId);
                continue;
            }
            Notification saved = persistNotification(user, NotificationType.DRIVE_ANNOUNCEMENT,
                    title, message, "DRIVE", driveId);
            NotificationDto dto = entityMapper.toNotificationDto(saved);

            // WebSocket only — no email for drive announcements
            webSocketNotificationService.sendToUser(userId, dto);
        }
    }

    // -----------------------------------------------------------------------
    // Read / update operations
    // -----------------------------------------------------------------------

    /**
     * Marks a single notification as read.
     *
     * <p>Ownership is verified — a user can only mark their own notifications.</p>
     *
     * @param notificationId the UUID of the notification to mark read
     * @param currentUserId  the UUID of the currently authenticated user
     * @return the updated {@link NotificationDto}
     * @throws EntityNotFoundException      if no notification exists with that id
     * @throws SecurityException            if the notification belongs to another user
     */
    public NotificationDto markAsRead(String notificationId, String currentUserId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Notification", notificationId));

        if (!notification.getUser().getId().equals(currentUserId)) {
            throw new SecurityException(
                    "User " + currentUserId + " is not the owner of notification " + notificationId);
        }

        notification.setRead(true);
        Notification saved = notificationRepository.save(notification);
        log.debug("Notification {} marked as read by user={}", notificationId, currentUserId);
        return entityMapper.toNotificationDto(saved);
    }

    /**
     * Marks every unread notification for the given user as read.
     *
     * @param currentUserId the UUID of the currently authenticated user
     * @return the number of notifications that were updated
     */
    public int markAllAsRead(String currentUserId) {
        List<Notification> unread = notificationRepository.findByUserIdAndIsReadFalse(currentUserId);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
        log.debug("Marked {} notifications as read for userId={}", unread.size(), currentUserId);
        return unread.size();
    }

    /**
     * Returns the count of unread notifications for the given user.
     *
     * @param currentUserId the UUID of the currently authenticated user
     * @return unread notification count
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(String currentUserId) {
        return notificationRepository.countByUserIdAndIsReadFalse(currentUserId);
    }

    /**
     * Returns a paged list of notifications for the given user, ordered newest-first.
     *
     * @param userId   the UUID of the user whose notifications to fetch
     * @param pageable pagination and sorting parameters
     * @return a {@link PagedResponse} of {@link NotificationDto} objects
     */
    @Transactional(readOnly = true)
    public PagedResponse<NotificationDto> getNotificationsForUser(String userId, Pageable pageable) {
        // Use the findAll with specification or the custom method + manual paging
        List<Notification> all = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);

        int total = all.size();
        int start = (int) pageable.getOffset();
        int end   = Math.min(start + pageable.getPageSize(), total);
        List<Notification> slice = (start >= total) ? List.of() : all.subList(start, end);

        List<NotificationDto> content = slice.stream()
                .map(entityMapper::toNotificationDto)
                .toList();

        Page<Notification> page = new PageImpl<>(slice, pageable, total);
        return entityMapper.toPagedResponse(page, content);
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    /**
     * Core orchestration helper: persist → WebSocket → email (async).
     *
     * <p>Resolves the {@link User} entity from the repository, then delegates
     * to {@link #persistNotification} and fires the outbound channels.</p>
     *
     * @param userId        the UUID of the target {@link User}
     * @param type          the notification type
     * @param title         short notification title
     * @param message       full notification body
     * @param referenceType the entity type the notification refers to (e.g. {@code "APPLICATION"})
     * @param referenceId   the UUID of the referenced entity
     * @return the persisted and mapped {@link NotificationDto}
     */
    private NotificationDto createAndDeliver(String userId,
                                             NotificationType type,
                                             String title,
                                             String message,
                                             String referenceType,
                                             String referenceId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User", userId));

        Notification saved = persistNotification(user, type, title, message,
                referenceType, referenceId);
        NotificationDto dto = entityMapper.toNotificationDto(saved);

        // Step 2: real-time WebSocket push
        webSocketNotificationService.sendToUser(userId, dto);

        // Step 3: fire-and-forget email (never throws)
        try {
            String htmlBody = buildEmailHtml(title, message);
            emailNotificationService.sendAsync(
                    user.getEmail(),
                    user.getFullName(),
                    title,
                    htmlBody
            );
        } catch (Exception ex) {
            // Email failure must never roll back the DB transaction
            log.warn("Unexpected error while queuing notification email for userId={}: {}",
                    userId, ex.getMessage());
        }

        return dto;
    }

    /**
     * Creates and saves a {@link Notification} entity to the database.
     *
     * @param user          the owning {@link User}
     * @param type          the notification type
     * @param title         short notification title
     * @param message       full notification body
     * @param referenceType entity type label
     * @param referenceId   entity UUID
     * @return the saved {@link Notification}
     */
    private Notification persistNotification(User user,
                                             NotificationType type,
                                             String title,
                                             String message,
                                             String referenceType,
                                             String referenceId) {
        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .build();

        Notification saved = notificationRepository.save(notification);
        log.debug("Persisted notification id={} type={} for userId={}",
                saved.getId(), type, user.getId());
        return saved;
    }

    /**
     * Wraps a notification title and message in a minimal HTML email template.
     *
     * @param title   the notification title used as the email heading
     * @param message the notification body text
     * @return an HTML string suitable for the email {@code content[].value} field
     */
    private String buildEmailHtml(String title, String message) {
        return "<!DOCTYPE html>"
             + "<html lang=\"en\"><head><meta charset=\"UTF-8\">"
             + "<title>" + escapeHtml(title) + "</title></head>"
             + "<body style=\"font-family:Arial,sans-serif;color:#333;padding:20px;\">"
             + "<h2 style=\"color:#1a73e8;\">" + escapeHtml(title) + "</h2>"
             + "<p>" + escapeHtml(message) + "</p>"
             + "<hr style=\"border:none;border-top:1px solid #e0e0e0;margin-top:30px;\"/>"
             + "<p style=\"font-size:12px;color:#999;\">Campus Placement Portal — "
             + "This is an automated notification. Please do not reply.</p>"
             + "</body></html>";
    }

    /**
     * Minimal HTML escaping to prevent injection in email bodies.
     *
     * @param text raw text; {@code null} is treated as an empty string
     * @return the escaped string
     */
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#x27;");
    }
}
