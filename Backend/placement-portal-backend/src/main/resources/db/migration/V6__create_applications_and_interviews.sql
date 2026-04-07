-- V6: Applications and Interviews

CREATE TABLE applications (
    id            CHAR(36) NOT NULL,
    student_id    CHAR(36) NOT NULL,
    job_id        CHAR(36)          DEFAULT NULL,
    internship_id CHAR(36)          DEFAULT NULL,
    resume_id     CHAR(36)          DEFAULT NULL,
    cover_letter  TEXT              DEFAULT NULL,
    status        ENUM('SUBMITTED','UNDER_REVIEW','SHORTLISTED','INTERVIEW_SCHEDULED',
                       'OFFERED','ACCEPTED','REJECTED','WITHDRAWN') NOT NULL DEFAULT 'SUBMITTED',
    applied_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_application_student_job        (student_id, job_id),
    UNIQUE KEY uq_application_student_internship (student_id, internship_id),
    INDEX idx_applications_student_id    (student_id),
    INDEX idx_applications_job_id        (job_id),
    INDEX idx_applications_internship_id (internship_id),
    INDEX idx_applications_status        (status),
    CONSTRAINT fk_applications_student    FOREIGN KEY (student_id)    REFERENCES student_profiles (id)  ON DELETE CASCADE,
    CONSTRAINT fk_applications_job        FOREIGN KEY (job_id)        REFERENCES jobs (id)              ON DELETE SET NULL,
    CONSTRAINT fk_applications_internship FOREIGN KEY (internship_id) REFERENCES internships (id)       ON DELETE SET NULL,
    CONSTRAINT fk_applications_resume     FOREIGN KEY (resume_id)     REFERENCES resumes (id)           ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE interviews (
    id             CHAR(36)  NOT NULL,
    application_id CHAR(36)  NOT NULL,
    round_number   TINYINT   NOT NULL DEFAULT 1,
    interview_type ENUM('PHONE','VIDEO','ON_SITE','TECHNICAL','HR') NOT NULL DEFAULT 'TECHNICAL',
    scheduled_at   DATETIME  NOT NULL,
    duration_mins  INT                DEFAULT 60,
    meeting_link   VARCHAR(500)       DEFAULT NULL,
    location       VARCHAR(255)       DEFAULT NULL,
    status         ENUM('SCHEDULED','COMPLETED','CANCELLED','NO_SHOW') NOT NULL DEFAULT 'SCHEDULED',
    feedback       TEXT               DEFAULT NULL,
    score          DECIMAL(5,2)       DEFAULT NULL,
    created_at     DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_interviews_application_id (application_id),
    INDEX idx_interviews_scheduled_at   (scheduled_at),
    INDEX idx_interviews_status         (status),
    CONSTRAINT fk_interviews_application FOREIGN KEY (application_id) REFERENCES applications (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
