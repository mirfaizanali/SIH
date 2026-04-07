-- V2: Role-specific profile tables

CREATE TABLE faculty_profiles (
    id            CHAR(36)     NOT NULL,
    user_id       CHAR(36)     NOT NULL,
    employee_id   VARCHAR(50)  NOT NULL,
    department    VARCHAR(100) NOT NULL,
    designation   VARCHAR(100)          DEFAULT NULL,
    phone         VARCHAR(20)           DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_faculty_user_id (user_id),
    UNIQUE KEY uq_faculty_employee_id (employee_id),
    CONSTRAINT fk_faculty_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE placement_officer_profiles (
    id            CHAR(36)     NOT NULL,
    user_id       CHAR(36)     NOT NULL,
    employee_id   VARCHAR(50)  NOT NULL,
    department    VARCHAR(100)          DEFAULT NULL,
    phone         VARCHAR(20)           DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_officer_user_id (user_id),
    UNIQUE KEY uq_officer_employee_id (employee_id),
    CONSTRAINT fk_officer_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE employer_profiles (
    id               CHAR(36)     NOT NULL,
    user_id          CHAR(36)     NOT NULL,
    company_name     VARCHAR(255) NOT NULL,
    company_website  VARCHAR(255)          DEFAULT NULL,
    industry         VARCHAR(100)          DEFAULT NULL,
    company_size     ENUM('STARTUP','SMALL','MEDIUM','LARGE','ENTERPRISE') DEFAULT NULL,
    hr_contact_name  VARCHAR(255)          DEFAULT NULL,
    hr_contact_phone VARCHAR(20)           DEFAULT NULL,
    is_verified      TINYINT(1)   NOT NULL DEFAULT 0,
    logo_url         VARCHAR(500)          DEFAULT NULL,
    description      TEXT                  DEFAULT NULL,
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_employer_user_id (user_id),
    INDEX idx_employer_is_verified (is_verified),
    CONSTRAINT fk_employer_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE student_profiles (
    id                  CHAR(36)       NOT NULL,
    user_id             CHAR(36)       NOT NULL,
    roll_number         VARCHAR(50)             DEFAULT NULL,
    department          VARCHAR(100)            DEFAULT NULL,
    batch_year          YEAR                    DEFAULT NULL,
    cgpa                DECIMAL(4,2)            DEFAULT NULL,
    phone               VARCHAR(20)             DEFAULT NULL,
    linkedin_url        VARCHAR(255)            DEFAULT NULL,
    github_url          VARCHAR(255)            DEFAULT NULL,
    bio                 TEXT                    DEFAULT NULL,
    preferred_locations VARCHAR(500)            DEFAULT NULL,
    preferred_job_types VARCHAR(200)            DEFAULT NULL,
    is_placed           TINYINT(1)     NOT NULL DEFAULT 0,
    placement_package   DECIMAL(12,2)           DEFAULT NULL,
    placed_company      VARCHAR(255)            DEFAULT NULL,
    faculty_mentor_id   CHAR(36)                DEFAULT NULL,
    created_at          DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_student_user_id (user_id),
    UNIQUE KEY uq_student_roll_number (roll_number),
    INDEX idx_student_department (department),
    INDEX idx_student_batch_year (batch_year),
    INDEX idx_student_cgpa (cgpa),
    INDEX idx_student_is_placed (is_placed),
    CONSTRAINT fk_student_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_student_faculty_mentor FOREIGN KEY (faculty_mentor_id) REFERENCES faculty_profiles (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
