-- V14: Make employee_id and department nullable in faculty and officer profiles
-- These columns were NOT NULL but the profile shell created during user registration
-- intentionally omits them (they are filled in later by the user).
-- Also drop UNIQUE constraints on employee_id because MySQL treats each NULL as
-- distinct, but it is cleaner to enforce uniqueness only on non-NULL values via
-- a partial-style approach; dropping the unique index allows multiple NULL rows.

ALTER TABLE faculty_profiles
    DROP INDEX uq_faculty_employee_id,
    MODIFY COLUMN employee_id  VARCHAR(50)  NULL DEFAULT NULL,
    MODIFY COLUMN department   VARCHAR(100) NULL DEFAULT NULL;

ALTER TABLE placement_officer_profiles
    DROP INDEX uq_officer_employee_id,
    MODIFY COLUMN employee_id  VARCHAR(50)  NULL DEFAULT NULL;
