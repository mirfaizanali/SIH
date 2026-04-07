-- V8: Notifications

CREATE TABLE notifications (
    id             CHAR(36)     NOT NULL,
    user_id        CHAR(36)     NOT NULL,
    type           ENUM('APPLICATION_STATUS','INTERVIEW_SCHEDULED','NEW_JOB',
                        'REPORT_FEEDBACK','DRIVE_ANNOUNCEMENT','SYSTEM','REMINDER') NOT NULL,
    title          VARCHAR(255) NOT NULL,
    message        TEXT         NOT NULL,
    is_read        TINYINT(1)   NOT NULL DEFAULT 0,
    reference_type VARCHAR(50)           DEFAULT NULL,
    reference_id   CHAR(36)              DEFAULT NULL,
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_notifications_user_id   (user_id),
    INDEX idx_notifications_is_read   (is_read),
    INDEX idx_notifications_created   (created_at),
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
