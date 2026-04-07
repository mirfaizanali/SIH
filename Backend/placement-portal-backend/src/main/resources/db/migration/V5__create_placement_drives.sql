-- V5: Placement drives and drive-job junction

CREATE TABLE placement_drives (
    id           CHAR(36)     NOT NULL,
    employer_id  CHAR(36)     NOT NULL,
    organized_by CHAR(36)              DEFAULT NULL,
    title        VARCHAR(255) NOT NULL,
    drive_date   DATE         NOT NULL,
    venue        VARCHAR(255)          DEFAULT NULL,
    mode         ENUM('ON_CAMPUS','OFF_CAMPUS','VIRTUAL') NOT NULL DEFAULT 'ON_CAMPUS',
    status       ENUM('SCHEDULED','ONGOING','COMPLETED','CANCELLED') NOT NULL DEFAULT 'SCHEDULED',
    description  TEXT                  DEFAULT NULL,
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_drives_employer_id (employer_id),
    INDEX idx_drives_status (status),
    INDEX idx_drives_drive_date (drive_date),
    CONSTRAINT fk_drives_employer  FOREIGN KEY (employer_id)  REFERENCES employer_profiles (id)          ON DELETE CASCADE,
    CONSTRAINT fk_drives_organized FOREIGN KEY (organized_by) REFERENCES placement_officer_profiles (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE drive_jobs (
    drive_id CHAR(36) NOT NULL,
    job_id   CHAR(36) NOT NULL,
    PRIMARY KEY (drive_id, job_id),
    CONSTRAINT fk_drive_jobs_drive FOREIGN KEY (drive_id) REFERENCES placement_drives (id) ON DELETE CASCADE,
    CONSTRAINT fk_drive_jobs_job   FOREIGN KEY (job_id)   REFERENCES jobs (id)             ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
