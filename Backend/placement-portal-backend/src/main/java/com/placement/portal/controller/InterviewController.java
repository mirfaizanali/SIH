package com.placement.portal.controller;

import com.placement.portal.dto.request.InterviewCreateRequest;
import com.placement.portal.dto.request.InterviewFeedbackRequest;
import com.placement.portal.dto.response.ApiResponse;
import com.placement.portal.dto.response.InterviewDto;
import com.placement.portal.service.placement.InterviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interviews")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    /** POST /api/interviews — schedules a new interview round. */
    @PostMapping
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<ApiResponse<InterviewDto>> scheduleInterview(
            @Valid @RequestBody InterviewCreateRequest req) {
        InterviewDto created = interviewService.scheduleInterview(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Interview scheduled", created));
    }

    /** GET /api/interviews/{id} — retrieves an interview by UUID. */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<InterviewDto>> getInterviewById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(interviewService.getInterviewById(id)));
    }

    /** GET /api/interviews/application/{applicationId} — all rounds for an application. */
    @GetMapping("/application/{applicationId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<InterviewDto>>> getInterviewsForApplication(
            @PathVariable String applicationId) {
        return ResponseEntity.ok(
                ApiResponse.success(interviewService.getInterviewsForApplication(applicationId)));
    }

    /** PATCH /api/interviews/{id}/feedback — records feedback and outcome. */
    @PatchMapping("/{id}/feedback")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<ApiResponse<InterviewDto>> updateInterviewFeedback(
            @PathVariable String id,
            @Valid @RequestBody InterviewFeedbackRequest req) {
        return ResponseEntity.ok(
                ApiResponse.success(interviewService.updateInterviewFeedback(id, req)));
    }

    /** DELETE /api/interviews/{id} — cancels an interview. */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<ApiResponse<Void>> cancelInterview(@PathVariable String id) {
        interviewService.cancelInterview(id);
        return ResponseEntity.ok(ApiResponse.success("Interview cancelled", null));
    }
}
