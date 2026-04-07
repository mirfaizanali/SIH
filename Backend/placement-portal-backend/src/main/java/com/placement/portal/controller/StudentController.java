package com.placement.portal.controller;

import com.placement.portal.dto.request.StudentProfileUpdateRequest;
import com.placement.portal.dto.response.ApiResponse;
import com.placement.portal.dto.response.PagedResponse;
import com.placement.portal.dto.response.StudentProfileDto;
import com.placement.portal.mapper.EntityMapper;
import com.placement.portal.service.user.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;
    private final EntityMapper entityMapper;

    /** GET /api/students/me — returns the authenticated student's own profile. */
    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<StudentProfileDto>> getMyProfile() {
        return ResponseEntity.ok(ApiResponse.success(studentService.getMyProfile()));
    }

    /** PUT /api/students/me — updates the authenticated student's mutable profile fields. */
    @PutMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<StudentProfileDto>> updateMyProfile(
            @RequestBody StudentProfileUpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.success(studentService.updateMyProfile(req)));
    }

    /** GET /api/students/{id} — retrieves any student profile by its UUID. */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('FACULTY_MENTOR', 'PLACEMENT_OFFICER', 'ADMIN')")
    public ResponseEntity<ApiResponse<StudentProfileDto>> getStudentById(
            @PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(studentService.getStudentById(id)));
    }

    /** GET /api/students — paginated, filterable list of all students. */
    @GetMapping
    @PreAuthorize("hasAnyRole('PLACEMENT_OFFICER', 'ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<StudentProfileDto>>> getAllStudents(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) Integer batchYear,
            @RequestParam(required = false) Boolean isPlaced,
            @PageableDefault(size = 10) Pageable pageable) {

        Page<StudentProfileDto> page = studentService.getAllStudents(
                department, batchYear, isPlaced, pageable);
        PagedResponse<StudentProfileDto> response = entityMapper.toPagedResponse(
                page, page.getContent().stream()
                        .filter(dto -> dto != null).toList());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /** PUT /api/students/{id}/mentor — assigns a faculty mentor to a student. */
    @PutMapping("/{id}/mentor")
    @PreAuthorize("hasAnyRole('PLACEMENT_OFFICER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> assignFacultyMentor(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        String facultyProfileId = body.get("facultyProfileId");
        studentService.assignFacultyMentor(id, facultyProfileId);
        return ResponseEntity.ok(ApiResponse.success("Faculty mentor assigned", null));
    }
}
