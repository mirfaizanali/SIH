package com.placement.portal.controller;

import com.placement.portal.dto.request.ApplicationRequest;
import com.placement.portal.dto.request.ApplicationStatusUpdateRequest;
import com.placement.portal.dto.response.ApiResponse;
import com.placement.portal.dto.response.ApplicationDto;
import com.placement.portal.dto.response.PagedResponse;
import com.placement.portal.mapper.EntityMapper;
import com.placement.portal.service.placement.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;
    private final EntityMapper entityMapper;

    /** POST /api/applications — submits a new application. */
    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<ApplicationDto>> apply(
            @RequestBody ApplicationRequest req) {
        ApplicationDto created = applicationService.applyToJob(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Application submitted", created));
    }

    /** GET /api/applications/my — returns the authenticated student's applications. */
    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<PagedResponse<ApplicationDto>>> getMyApplications(
            @PageableDefault(size = 10) Pageable pageable) {

        Page<ApplicationDto> page = applicationService.getMyApplications(pageable);
        PagedResponse<ApplicationDto> response = entityMapper.toPagedResponse(page, page.getContent());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /** GET /api/applications/{id} — retrieves a single application (ownership checked in service). */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ApplicationDto>> getApplicationById(
            @PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(applicationService.getApplicationById(id)));
    }

    /** GET /api/applications/job/{jobId} — returns all applications for a job. */
    @GetMapping("/job/{jobId}")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'PLACEMENT_OFFICER')")
    public ResponseEntity<ApiResponse<PagedResponse<ApplicationDto>>> getApplicationsForJob(
            @PathVariable String jobId,
            @PageableDefault(size = 10) Pageable pageable) {

        Page<ApplicationDto> page = applicationService.getApplicationsForJob(jobId, pageable);
        PagedResponse<ApplicationDto> response = entityMapper.toPagedResponse(page, page.getContent());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /** PATCH /api/applications/{id}/status — updates an application's status. */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'PLACEMENT_OFFICER')")
    public ResponseEntity<ApiResponse<ApplicationDto>> updateApplicationStatus(
            @PathVariable String id,
            @Valid @RequestBody ApplicationStatusUpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.success(
                applicationService.updateApplicationStatus(id, req)));
    }

    /** DELETE /api/applications/{id} — student withdraws their own application. */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Void>> withdrawApplication(@PathVariable String id) {
        applicationService.withdrawApplication(id);
        return ResponseEntity.ok(ApiResponse.success("Application withdrawn", null));
    }
}
