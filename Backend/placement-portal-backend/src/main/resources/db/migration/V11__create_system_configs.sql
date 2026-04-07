-- V11: System configuration key-value store

CREATE TABLE system_configs (
    config_key   VARCHAR(100) NOT NULL,
    config_value TEXT         NOT NULL,
    description  TEXT                  DEFAULT NULL,
    updated_by   CHAR(36)              DEFAULT NULL,
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
