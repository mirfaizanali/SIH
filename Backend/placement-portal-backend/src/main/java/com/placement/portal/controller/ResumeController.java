package com.placement.portal.controller;

import com.placement.portal.dto.response.ApiResponse;
import com.placement.portal.dto.response.ResumeDto;
import com.placement.portal.service.resume.ResumeStorageService;
import com.placement.portal.util.SecurityUtils;
import com.placement.portal.repository.StudentProfileRepository;
import com.placement.portal.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeStorageService resumeStorageService;
    private final StudentProfileRepository studentProfileRepository;
    private final SecurityUtils securityUtils;

    /**
     * POST /api/resumes — uploads a résumé file for the authenticated student.
     * The studentProfileId is resolved from the current user's session.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<ResumeDto>> uploadResume(
            @RequestPart("file") MultipartFile file) {

        String userId = securityUtils.getCurrentUserId();
        String studentProfileId = studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "StudentProfile for user " + userId + " not found"))
                .getId();

        ResumeDto created = resumeStorageService.uploadResume(file, studentProfileId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Resume uploaded", created));
    }

    /** GET /api/resumes/my — returns all résumés for the authenticated student. */
    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<ResumeDto>>> getMyResumes() {
        return ResponseEntity.ok(ApiResponse.success(resumeStorageService.getMyResumes()));
    }

    /** PATCH /api/resumes/{id}/primary — marks a résumé as the primary one. */
    @PatchMapping("/{id}/primary")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Void>> setPrimaryResume(@PathVariable String id) {
        resumeStorageService.setPrimaryResume(id);
        return ResponseEntity.ok(ApiResponse.success("Primary résumé updated", null));
    }

    /** DELETE /api/resumes/{id} — deletes a résumé file and its database record. */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Void>> deleteResume(@PathVariable String id) {
        resumeStorageService.deleteResume(id);
        return ResponseEntity.ok(ApiResponse.success("Resume deleted", null));
    }
}
