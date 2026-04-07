-- V7: Internship enrollments and progress reports

CREATE TABLE internship_enrollments (
    id                CHAR(36) NOT NULL,
    student_id        CHAR(36) NOT NULL,
    internship_id     CHAR(36) NOT NULL,
    faculty_mentor_id CHAR(36)          DEFAULT NULL,
    start_date        DATE              DEFAULT NULL,
    end_date          DATE              DEFAULT NULL,
    status            ENUM('ONGOING','COMPLETED','TERMINATED') NOT NULL DEFAULT 'ONGOING',
    offer_letter_path VARCHAR(500)      DEFAULT NULL,
    created_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_enrollment_student_internship (student_id, internship_id),
    INDEX idx_enrollments_student_id    (student_id),
    INDEX idx_enrollments_internship_id (internship_id),
    INDEX idx_enrollments_mentor_id     (faculty_mentor_id),
    CONSTRAINT fk_enrollments_student  FOREIGN KEY (student_id)        REFERENCES student_profiles (id)  ON DELETE CASCADE,
    CONSTRAINT fk_enrollments_intern   FOREIGN KEY (internship_id)     REFERENCES internships (id)       ON DELETE CASCADE,
    CONSTRAINT fk_enrollments_mentor   FOREIGN KEY (faculty_mentor_id) REFERENCES faculty_profiles (id)  ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE reports (
    id                CHAR(36)     NOT NULL,
    enrollment_id     CHAR(36)     NOT NULL,
    report_type       ENUM('WEEKLY','MONTHLY','FINAL') NOT NULL DEFAULT 'WEEKLY',
    title             VARCHAR(255) NOT NULL,
    content           TEXT                  DEFAULT NULL,
    file_path         VARCHAR(500)          DEFAULT NULL,
    submitted_at      DATETIME              DEFAULT NULL,
    status            ENUM('DRAFT','SUBMITTED','APPROVED','REVISION_REQUESTED') NOT NULL DEFAULT 'DRAFT',
    reviewer_id       CHAR(36)              DEFAULT NULL,
    reviewer_comments TEXT                  DEFAULT NULL,
    reviewed_at       DATETIME              DEFAULT NULL,
    created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_reports_enrollment_id (enrollment_id),
    INDEX idx_reports_status        (status),
    INDEX idx_reports_reviewer_id   (reviewer_id),
    CONSTRAINT fk_reports_enrollment FOREIGN KEY (enrollment_id) REFERENCES internship_enrollments (id) ON DELETE CASCADE,
    CONSTRAINT fk_reports_reviewer   FOREIGN KEY (reviewer_id)   REFERENCES faculty_profiles (id)       ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
