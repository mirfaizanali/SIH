-- V3: Skill tags master table and student skills junction

CREATE TABLE skill_tags (
    id       INT          NOT NULL AUTO_INCREMENT,
    name     VARCHAR(100) NOT NULL,
    category VARCHAR(50)           DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_skill_tags_name (name),
    INDEX idx_skill_tags_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE student_skills (
    student_profile_id CHAR(36)    NOT NULL,
    skill_tag_id       INT         NOT NULL,
    proficiency_level  ENUM('BEGINNER','INTERMEDIATE','ADVANCED','EXPERT') NOT NULL DEFAULT 'BEGINNER',
    PRIMARY KEY (student_profile_id, skill_tag_id),
    CONSTRAINT fk_student_skills_student FOREIGN KEY (student_profile_id) REFERENCES student_profiles (id) ON DELETE CASCADE,
    CONSTRAINT fk_student_skills_tag    FOREIGN KEY (skill_tag_id)        REFERENCES skill_tags (id)        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
