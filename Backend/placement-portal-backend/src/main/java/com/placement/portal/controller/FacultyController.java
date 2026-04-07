package com.placement.portal.controller;

import com.placement.portal.dto.response.ApiResponse;
import com.placement.portal.dto.response.FacultyProfileDto;
import com.placement.portal.dto.response.StudentProfileDto;
import com.placement.portal.service.user.FacultyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/faculty")
@RequiredArgsConstructor
public class FacultyController {

    private final FacultyService facultyService;

    /** GET /api/faculty/me — returns the authenticated faculty member's own profile. */
    @GetMapping("/me")
    @PreAuthorize("hasRole('FACULTY_MENTOR')")
    public ResponseEntity<ApiResponse<FacultyProfileDto>> getMyProfile() {
        return ResponseEntity.ok(ApiResponse.success(facultyService.getMyProfile()));
    }

    /** GET /api/faculty/me/mentees — returns students assigned to the authenticated faculty member. */
    @GetMapping("/me/mentees")
    @PreAuthorize("hasRole('FACULTY_MENTOR')")
    public ResponseEntity<ApiResponse<List<StudentProfileDto>>> getMyMentees() {
        return ResponseEntity.ok(ApiResponse.success(facultyService.getMyMentees()));
    }

    /** GET /api/faculty/{id} — retrieves a faculty profile by its UUID. */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PLACEMENT_OFFICER', 'ADMIN')")
    public ResponseEntity<ApiResponse<FacultyProfileDto>> getFacultyById(
            @PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(facultyService.getFacultyById(id)));
    }
}
