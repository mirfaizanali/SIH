-- V1: Users table with role-based access

CREATE TABLE users (
    id            CHAR(36)     NOT NULL,
    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255)          DEFAULT NULL,
    full_name     VARCHAR(255) NOT NULL,
    role          ENUM('STUDENT','FACULTY_MENTOR','PLACEMENT_OFFICER','EMPLOYER','ADMIN') NOT NULL,
    oauth2_provider VARCHAR(50)         DEFAULT NULL,
    oauth2_id     VARCHAR(255)          DEFAULT NULL,
    is_active     TINYINT(1)   NOT NULL DEFAULT 1,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_users_email (email),
    INDEX idx_users_role (role),
    INDEX idx_users_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
