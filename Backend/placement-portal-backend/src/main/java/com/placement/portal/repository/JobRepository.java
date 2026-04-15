package com.placement.portal.repository;

import com.placement.portal.domain.Job;
import com.placement.portal.domain.enums.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, String> {

    List<Job> findByStatus(JobStatus status);

    List<Job> findByEmployerId(String employerId);

    @org.springframework.data.jpa.repository.Query("SELECT j FROM Job j WHERE j.status = :status AND (j.applicationDeadline IS NULL OR j.applicationDeadline >= :deadline)")
    Page<Job> findActiveJobs(@org.springframework.data.repository.query.Param("status") JobStatus status, @org.springframework.data.repository.query.Param("deadline") LocalDate deadline, Pageable pageable);

    // ---------------------------------------------------------------------------
    // Analytics queries
    // ---------------------------------------------------------------------------

    /** Returns the total number of jobs with the given status. */
    long countByStatus(JobStatus status);
}
