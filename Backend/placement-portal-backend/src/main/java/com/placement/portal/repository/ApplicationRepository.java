package com.placement.portal.repository;

import com.placement.portal.domain.Application;
import com.placement.portal.domain.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, String> {

    List<Application> findByStudentId(String studentId);

    List<Application> findByJobId(String jobId);

    List<Application> findByInternshipId(String internshipId);

    List<Application> findByStatus(ApplicationStatus status);

    boolean existsByStudentIdAndJobId(String studentId, String jobId);

    boolean existsByStudentIdAndInternshipId(String studentId, String internshipId);

    // ---------------------------------------------------------------------------
    // Analytics queries
    // ---------------------------------------------------------------------------

    /** Returns the total number of applications with the given status. */
    long countByStatus(ApplicationStatus status);
}
