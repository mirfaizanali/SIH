package com.placement.portal.service.analytics;

import com.placement.portal.domain.enums.ApplicationStatus;
import com.placement.portal.domain.enums.DriveStatus;
import com.placement.portal.domain.enums.InternshipStatus;
import com.placement.portal.domain.enums.JobStatus;
import com.placement.portal.repository.ApplicationRepository;
import com.placement.portal.repository.EmployerProfileRepository;
import com.placement.portal.repository.InternshipRepository;
import com.placement.portal.repository.JobRepository;
import com.placement.portal.repository.PlacementDriveRepository;
import com.placement.portal.repository.StudentProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Aggregates platform-wide statistics for the admin/placement-officer dashboard.
 *
 * <p>Results are cached under {@code analytics_dashboard} (15-minute TTL) to avoid
 * running heavy aggregation queries on every page load. The cache is evicted via
 * {@link com.placement.portal.controller.AnalyticsController#refreshDashboardCache()}.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final StudentProfileRepository  studentProfileRepository;
    private final ApplicationRepository     applicationRepository;
    private final JobRepository             jobRepository;
    private final InternshipRepository      internshipRepository;
    private final PlacementDriveRepository  placementDriveRepository;
    private final EmployerProfileRepository employerProfileRepository;

    // ---------------------------------------------------------------------------
    // Dashboard stats record
    // ---------------------------------------------------------------------------

    /**
     * Immutable snapshot of platform statistics returned by the dashboard endpoint.
     *
     * @param totalStudents        total number of registered students
     * @param placedStudents       students marked as placed
     * @param placementRate        percentage placed (0–100, 2 decimal places)
     * @param averagePackage       mean placement package across placed students (may be null)
     * @param activeJobs           jobs with status ACTIVE
     * @param activeInternships    internships with status ACTIVE
     * @param scheduledDrives      placement drives with status SCHEDULED
     * @param pendingApplications  applications with status SUBMITTED
     * @param totalEmployers       total employer profiles
     * @param verifiedEmployers    employer profiles marked as verified
     */
    public record DashboardStats(
            long       totalStudents,
            long       placedStudents,
            double     placementRate,
            BigDecimal averagePackage,
            long       activeJobs,
            long       activeInternships,
            long       scheduledDrives,
            long       pendingApplications,
            long       totalEmployers,
            long       verifiedEmployers
    ) {}

    // ---------------------------------------------------------------------------
    // Main aggregation — cached
    // ---------------------------------------------------------------------------

    /**
     * Computes and returns platform-wide dashboard statistics.
     *
     * <p>Results are cached for 15 minutes. Use
     * {@link com.placement.portal.controller.AnalyticsController#refreshDashboardCache()}
     * to force-evict the cache.</p>
     *
     * @return a {@link DashboardStats} snapshot
     */
    @Cacheable("analytics_dashboard")
    @Transactional(readOnly = true)
    public DashboardStats getDashboardStats() {
        log.info("Computing dashboard analytics (cache miss)");

        long totalStudents   = studentProfileRepository.count();
        long placedStudents  = studentProfileRepository.countByIsPlacedTrue();

        double placementRate = totalStudents > 0
                ? BigDecimal.valueOf((double) placedStudents / totalStudents * 100.0)
                        .setScale(2, RoundingMode.HALF_UP)
                        .doubleValue()
                : 0.0;

        BigDecimal averagePackage = studentProfileRepository.findAveragePackageOfPlacedStudents();

        long activeJobs           = jobRepository.countByStatus(JobStatus.ACTIVE);
        long activeInternships    = internshipRepository.countByStatus(InternshipStatus.ACTIVE);
        long scheduledDrives      = placementDriveRepository.countByStatus(DriveStatus.SCHEDULED);
        long pendingApplications  = applicationRepository.countByStatus(ApplicationStatus.SUBMITTED);
        long totalEmployers       = employerProfileRepository.count();
        long verifiedEmployers    = employerProfileRepository.countByIsVerifiedTrue();

        return new DashboardStats(
                totalStudents,
                placedStudents,
                placementRate,
                averagePackage,
                activeJobs,
                activeInternships,
                scheduledDrives,
                pendingApplications,
                totalEmployers,
                verifiedEmployers
        );
    }
}
