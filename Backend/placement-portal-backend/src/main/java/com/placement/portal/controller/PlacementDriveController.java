package com.placement.portal.controller;

import com.placement.portal.domain.enums.DriveStatus;
import com.placement.portal.dto.request.PlacementDriveCreateRequest;
import com.placement.portal.dto.response.ApiResponse;
import com.placement.portal.dto.response.PagedResponse;
import com.placement.portal.dto.response.PlacementDriveDto;
import com.placement.portal.mapper.EntityMapper;
import com.placement.portal.service.placement.PlacementDriveService;
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
@RequestMapping("/api/drives")
@RequiredArgsConstructor
public class PlacementDriveController {

    private final PlacementDriveService driveService;
    private final EntityMapper entityMapper;

    /** POST /api/drives — creates a new placement drive. */
    @PostMapping
    @PreAuthorize("hasRole('PLACEMENT_OFFICER')")
    public ResponseEntity<ApiResponse<PlacementDriveDto>> createDrive(
            @Valid @RequestBody PlacementDriveCreateRequest req) {
        PlacementDriveDto created = driveService.createDrive(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Placement drive created", created));
    }

    /** GET /api/drives — paginated list of drives (optionally filtered by status). */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PagedResponse<PlacementDriveDto>>> getAllDrives(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 10) Pageable pageable) {

        Page<PlacementDriveDto> page = driveService.getAllDrives(status, pageable);
        PagedResponse<PlacementDriveDto> response = entityMapper.toPagedResponse(page, page.getContent());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /** GET /api/drives/{id} — retrieves a placement drive by UUID. */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PlacementDriveDto>> getDriveById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(driveService.getDriveById(id)));
    }

    /** PATCH /api/drives/{id}/status — updates the status of a placement drive. */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('PLACEMENT_OFFICER')")
    public ResponseEntity<ApiResponse<PlacementDriveDto>> updateDriveStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        DriveStatus status = DriveStatus.valueOf(body.get("status").toUpperCase());
        return ResponseEntity.ok(ApiResponse.success(driveService.updateDriveStatus(id, status)));
    }
}
