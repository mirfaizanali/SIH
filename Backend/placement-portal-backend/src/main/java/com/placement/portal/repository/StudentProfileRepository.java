package com.placement.portal.repository;

import com.placement.portal.domain.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentProfileRepository extends JpaRepository<StudentProfile, String> {

    Optional<StudentProfile> findByUserId(String userId);

    Optional<StudentProfile> findByRollNumber(String rollNumber);

    List<StudentProfile> findByDepartmentAndBatchYear(String department, Integer batchYear);

    List<StudentProfile> findByFacultyMentorId(String facultyMentorId);

    List<StudentProfile> findByIsPlacedFalseAndCgpaGreaterThanEqual(BigDecimal minCgpa);

    // ---------------------------------------------------------------------------
    // Analytics queries
    // ---------------------------------------------------------------------------

    /** Counts how many students have been placed. */
    long countByIsPlacedTrue();

    /**
     * Returns the average placement package across all placed students.
     * Returns {@code null} if no students have been placed yet.
     */
    @Query("SELECT AVG(s.placementPackage) FROM StudentProfile s WHERE s.isPlaced = true")
    BigDecimal findAveragePackageOfPlacedStudents();
}
