package com.placement.portal.controller;

import com.placement.portal.dto.response.ApiResponse;
import com.placement.portal.dto.response.JobDto;
import com.placement.portal.dto.response.StudentProfileDto;
import com.placement.portal.service.recommendation.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST endpoints for the recommendation engine.
 *
 * <ul>
 *   <li>{@code GET /api/recommendations/jobs}     — personalised job recommendations for students</li>
 *   <li>{@code GET /api/recommendations/students}  — top candidate students for a job posting</li>
 * </ul>
 */
@Slf4j
@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    // ---------------------------------------------------------------------------
    // Student: recommended jobs
    // ---------------------------------------------------------------------------

    /**
     * Returns a ranked list of active job recommendations for the currently
     * authenticated student.
     *
     * <p>Each item includes a {@code matchScore} field (0–100) indicating
     * how well the student's profile aligns with the posting.</p>
     *
     * @param limit maximum number of results (default 10, max controlled by service)
     * @return wrapped list of JobDtos with match scores
     */
    @GetMapping("/jobs")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<JobDto>>> getTopJobsForCurrentStudent(
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<JobDto> recommendations = recommendationService.getTopJobsForCurrentStudent(limit);
        return ResponseEntity.ok(ApiResponse.success(
                "Job recommendations retrieved successfully", recommendations));
    }

    // ---------------------------------------------------------------------------
    // Employer / Placement Officer: top student candidates
    // ---------------------------------------------------------------------------

    /**
     * Returns a ranked list of student candidates best matched to the given job.
     *
     * <p>Accessible to employers and placement officers only.</p>
     *
     * @param jobId ID of the job to find candidates for
     * @param limit maximum number of results (default 20)
     * @return wrapped list of StudentProfileDtos
     */
    @GetMapping("/students")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'PLACEMENT_OFFICER')")
    public ResponseEntity<ApiResponse<List<StudentProfileDto>>> getTopStudentsForJob(
            @RequestParam String jobId,
            @RequestParam(defaultValue = "20") int limit
    ) {
        List<StudentProfileDto> candidates = recommendationService.getTopStudentsForJob(jobId, limit);
        return ResponseEntity.ok(ApiResponse.success(
                "Student recommendations retrieved successfully", candidates));
    }
}
