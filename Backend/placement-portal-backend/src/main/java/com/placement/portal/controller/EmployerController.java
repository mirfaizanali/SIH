package com.placement.portal.controller;

import com.placement.portal.dto.request.EmployerProfileUpdateRequest;
import com.placement.portal.dto.response.ApiResponse;
import com.placement.portal.dto.response.EmployerProfileDto;
import com.placement.portal.dto.response.PagedResponse;
import com.placement.portal.mapper.EntityMapper;
import com.placement.portal.service.user.EmployerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employers")
@RequiredArgsConstructor
public class EmployerController {

    private final EmployerService employerService;
    private final EntityMapper entityMapper;

    /** GET /api/employers/me — returns the authenticated employer's own profile. */
    @GetMapping("/me")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<ApiResponse<EmployerProfileDto>> getMyProfile() {
        return ResponseEntity.ok(ApiResponse.success(employerService.getMyProfile()));
    }

    /** PUT /api/employers/me — updates the authenticated employer's profile. */
    @PutMapping("/me")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<ApiResponse<EmployerProfileDto>> updateMyProfile(
            @RequestBody EmployerProfileUpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.success(employerService.updateMyProfile(req)));
    }

    /** GET /api/employers — paginated list of employers (optionally filtered by verification status). */
    @GetMapping
    @PreAuthorize("hasAnyRole('PLACEMENT_OFFICER', 'ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<EmployerProfileDto>>> getAllEmployers(
            @RequestParam(required = false) Boolean isVerified,
            @PageableDefault(size = 10) Pageable pageable) {

        Page<EmployerProfileDto> page = employerService.getAllEmployers(isVerified, pageable);
        PagedResponse<EmployerProfileDto> response = entityMapper.toPagedResponse(
                page, page.getContent().stream().filter(dto -> dto != null).toList());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /** PUT /api/employers/{id}/verify — marks an employer as verified. */
    @PutMapping("/{id}/verify")
    @PreAuthorize("hasAnyRole('PLACEMENT_OFFICER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> verifyEmployer(@PathVariable String id) {
        employerService.verifyEmployer(id);
        return ResponseEntity.ok(ApiResponse.success("Employer verified", null));
    }
}
