package com.placement.portal.controller;

import com.placement.portal.domain.enums.JobStatus;
import com.placement.portal.dto.request.JobCreateRequest;
import com.placement.portal.dto.response.ApiResponse;
import com.placement.portal.dto.response.JobDto;
import com.placement.portal.dto.response.PagedResponse;
import com.placement.portal.mapper.EntityMapper;
import com.placement.portal.service.placement.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;
    private final EntityMapper entityMapper;

    /** POST /api/jobs — creates a new job posting. */
    @PostMapping
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<ApiResponse<JobDto>> createJob(
            @Valid @RequestBody JobCreateRequest req) {
        JobDto created = jobService.createJob(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Job created", created));
    }

    /** GET /api/jobs — public list of active jobs with optional filters. */
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<JobDto>>> getActiveJobs(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String experienceLevel,
            @PageableDefault(size = 10) Pageable pageable) {

        Page<JobDto> page = jobService.getActiveJobs(location, experienceLevel, pageable);
        PagedResponse<JobDto> response = entityMapper.toPagedResponse(
                page, page.getContent().stream().filter(dto -> dto != null).toList());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /** GET /api/jobs/{id} — public: retrieves a job by UUID. */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<JobDto>> getJobById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(jobService.getJobById(id)));
    }

    /** GET /api/jobs/my — returns the authenticated employer's own job postings. */
    @GetMapping("/my")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<ApiResponse<PagedResponse<JobDto>>> getMyJobs(
            @PageableDefault(size = 10) Pageable pageable) {

        Page<JobDto> page = jobService.getMyJobs(pageable);
        PagedResponse<JobDto> response = entityMapper.toPagedResponse(page, page.getContent());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /** PUT /api/jobs/{id} — updates a job posting. */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<ApiResponse<JobDto>> updateJob(
            @PathVariable String id,
            @Valid @RequestBody JobCreateRequest req) {
        return ResponseEntity.ok(ApiResponse.success(jobService.updateJob(id, req)));
    }

    /** PATCH /api/jobs/{id}/status — updates a job's status. */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'PLACEMENT_OFFICER')")
    public ResponseEntity<ApiResponse<Void>> updateJobStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        JobStatus status = JobStatus.valueOf(body.get("status").toUpperCase());
        jobService.updateJobStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Job status updated", null));
    }
}
