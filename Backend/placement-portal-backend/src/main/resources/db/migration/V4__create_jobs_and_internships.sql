-- V4: Jobs, Internships and their skill requirements

CREATE TABLE jobs (
    id                   CHAR(36)      NOT NULL,
    employer_id          CHAR(36)      NOT NULL,
    title                VARCHAR(255)  NOT NULL,
    description          TEXT                   DEFAULT NULL,
    location             VARCHAR(255)           DEFAULT NULL,
    job_type             ENUM('FULL_TIME','PART_TIME','CONTRACT') NOT NULL DEFAULT 'FULL_TIME',
    experience_level     ENUM('FRESHER','JUNIOR','MID','SENIOR') NOT NULL DEFAULT 'FRESHER',
    min_cgpa             DECIMAL(4,2)           DEFAULT NULL,
    salary_min           DECIMAL(12,2)          DEFAULT NULL,
    salary_max           DECIMAL(12,2)          DEFAULT NULL,
    openings_count       INT           NOT NULL DEFAULT 1,
    application_deadline DATE                   DEFAULT NULL,
    status               ENUM('DRAFT','ACTIVE','CLOSED','FILLED') NOT NULL DEFAULT 'DRAFT',
    created_at           DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_jobs_employer_id (employer_id),
    INDEX idx_jobs_status (status),
    INDEX idx_jobs_deadline (application_deadline),
    INDEX idx_jobs_experience_level (experience_level),
    CONSTRAINT fk_jobs_employer FOREIGN KEY (employer_id) REFERENCES employer_profiles (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE job_skills (
    job_id       CHAR(36) NOT NULL,
    skill_tag_id INT      NOT NULL,
    is_mandatory TINYINT(1) NOT NULL DEFAULT 1,
    PRIMARY KEY (job_id, skill_tag_id),
    CONSTRAINT fk_job_skills_job  FOREIGN KEY (job_id)       REFERENCES jobs (id)       ON DELETE CASCADE,
    CONSTRAINT fk_job_skills_tag  FOREIGN KEY (skill_tag_id) REFERENCES skill_tags (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE internships (
    id                   CHAR(36)      NOT NULL,
    employer_id          CHAR(36)      NOT NULL,
    title                VARCHAR(255)  NOT NULL,
    description          TEXT                   DEFAULT NULL,
    duration_months      TINYINT                DEFAULT NULL,
    stipend              DECIMAL(10,2)          DEFAULT NULL,
    location             VARCHAR(255)           DEFAULT NULL,
    is_remote            TINYINT(1)    NOT NULL DEFAULT 0,
    min_cgpa             DECIMAL(4,2)           DEFAULT NULL,
    application_deadline DATE                   DEFAULT NULL,
    status               ENUM('DRAFT','ACTIVE','CLOSED') NOT NULL DEFAULT 'DRAFT',
    created_at           DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_internships_employer_id (employer_id),
    INDEX idx_internships_status (status),
    CONSTRAINT fk_internships_employer FOREIGN KEY (employer_id) REFERENCES employer_profiles (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE resumes (
    id                 CHAR(36)     NOT NULL,
    student_profile_id CHAR(36)     NOT NULL,
    file_name          VARCHAR(255) NOT NULL,
    storage_path       VARCHAR(500) NOT NULL,
    file_size_bytes    BIGINT                DEFAULT NULL,
    content_type       VARCHAR(100)          DEFAULT NULL,
    is_primary         TINYINT(1)   NOT NULL DEFAULT 0,
    uploaded_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_resumes_student_profile_id (student_profile_id),
    CONSTRAINT fk_resumes_student FOREIGN KEY (student_profile_id) REFERENCES student_profiles (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
