package com.placement.portal.controller;

import com.placement.portal.domain.enums.InternshipStatus;
import com.placement.portal.dto.request.InternshipCreateRequest;
import com.placement.portal.dto.response.ApiResponse;
import com.placement.portal.dto.response.InternshipDto;
import com.placement.portal.dto.response.PagedResponse;
import com.placement.portal.mapper.EntityMapper;
import com.placement.portal.service.placement.InternshipService;
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
@RequestMapping("/api/internships")
@RequiredArgsConstructor
public class InternshipController {

    private final InternshipService internshipService;
    private final EntityMapper entityMapper;

    /** POST /api/internships — creates a new internship posting. */
    @PostMapping
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<ApiResponse<InternshipDto>> createInternship(
            @Valid @RequestBody InternshipCreateRequest req) {
        InternshipDto created = internshipService.createInternship(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Internship created", created));
    }

    /** GET /api/internships — public list of active internships. */
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<InternshipDto>>> getActiveInternships(
            @PageableDefault(size = 10) Pageable pageable) {

        Page<InternshipDto> page = internshipService.getActiveInternships(pageable);
        PagedResponse<InternshipDto> response = entityMapper.toPagedResponse(page, page.getContent());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /** GET /api/internships/{id} — public: retrieves an internship by UUID. */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InternshipDto>> getInternshipById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(internshipService.getInternshipById(id)));
    }

    /** GET /api/internships/my — returns the authenticated employer's own internship postings. */
    @GetMapping("/my")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<ApiResponse<PagedResponse<InternshipDto>>> getMyInternships(
            @PageableDefault(size = 10) Pageable pageable) {

        Page<InternshipDto> page = internshipService.getMyInternships(pageable);
        PagedResponse<InternshipDto> response = entityMapper.toPagedResponse(page, page.getContent());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /** PATCH /api/internships/{id}/status — updates an internship's status. */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'PLACEMENT_OFFICER')")
    public ResponseEntity<ApiResponse<Void>> updateInternshipStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        InternshipStatus status = InternshipStatus.valueOf(body.get("status").toUpperCase());
        internshipService.updateInternshipStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Internship status updated", null));
    }
}
