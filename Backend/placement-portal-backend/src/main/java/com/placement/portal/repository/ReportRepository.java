package com.placement.portal.repository;

import com.placement.portal.domain.Report;
import com.placement.portal.domain.enums.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, String> {

    List<Report> findByEnrollmentId(String enrollmentId);

    List<Report> findByReviewerId(String reviewerId);

    List<Report> findByStatus(ReportStatus status);
}
