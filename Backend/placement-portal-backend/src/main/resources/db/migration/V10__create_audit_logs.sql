-- V10: Audit logs for security and compliance

CREATE TABLE audit_logs (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    user_id     CHAR(36)              DEFAULT NULL,
    action      VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50)           DEFAULT NULL,
    entity_id   CHAR(36)              DEFAULT NULL,
    ip_address  VARCHAR(45)           DEFAULT NULL,
    user_agent  VARCHAR(500)          DEFAULT NULL,
    details     TEXT                  DEFAULT NULL,
    timestamp   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_audit_user_id   (user_id),
    INDEX idx_audit_action    (action),
    INDEX idx_audit_timestamp (timestamp),
    INDEX idx_audit_entity    (entity_type, entity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
