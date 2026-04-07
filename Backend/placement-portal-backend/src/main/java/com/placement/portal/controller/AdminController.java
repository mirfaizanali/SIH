package com.placement.portal.controller;

import com.placement.portal.domain.AuditLog;
import com.placement.portal.dto.request.RegisterRequest;
import com.placement.portal.dto.response.ApiResponse;
import com.placement.portal.dto.response.PagedResponse;
import com.placement.portal.dto.response.UserDto;
import com.placement.portal.mapper.EntityMapper;
import com.placement.portal.repository.AuditLogRepository;
import com.placement.portal.service.admin.AdminUserService;
import com.placement.portal.service.admin.SystemConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Administrative REST API.
 *
 * <p>All endpoints in this controller require the {@code ADMIN} role. The class-level
 * {@code @PreAuthorize} annotation covers every method; individual methods may further
 * restrict access if needed.</p>
 *
 * <ul>
 *   <li>User management  — list, view, activate, deactivate, reset-password</li>
 *   <li>System config    — list all, set, delete</li>
 *   <li>Audit logs       — paged retrieval</li>
 * </ul>
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminUserService    adminUserService;
    private final SystemConfigService systemConfigService;
    private final AuditLogRepository  auditLogRepository;
    private final EntityMapper        entityMapper;

    // ===========================================================================
    // User management
    // ===========================================================================

    /**
     * Returns a paginated list of all users, with optional filters for role and
     * active status.
     *
     * @param role     optional role filter (e.g. "STUDENT", "EMPLOYER")
     * @param isActive optional active-status filter
     * @param pageable pagination parameters (default page size 20)
     * @return paged list of UserDtos
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<PagedResponse<UserDto>>> getAllUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean isActive,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<UserDto> page = adminUserService.getAllUsers(role, isActive, pageable);
        PagedResponse<UserDto> paged = entityMapper.toPagedResponse(page, page.getContent());
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", paged));
    }

    /**
     * Creates a new user account with role-specific profile shell.
     *
     * @param request validated registration payload
     * @return created UserDto with 201 status
     */
    @PostMapping("/users")
    public ResponseEntity<ApiResponse<UserDto>> createUser(@Valid @RequestBody RegisterRequest request) {
        UserDto created = adminUserService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully", created));
    }

    /**
     * Returns the details of a single user by ID.
     *
     * @param id user UUID
     * @return UserDto wrapped in ApiResponse
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable String id) {
        UserDto user = adminUserService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", user));
    }

    /**
     * Activates the account of the specified user.
     *
     * @param id user UUID
     * @return success response
     */
    @PatchMapping("/users/{id}/activate")
    public ResponseEntity<ApiResponse<UserDto>> activateUser(@PathVariable String id) {
        adminUserService.activateUser(id);
        return ResponseEntity.ok(ApiResponse.success("User activated successfully", adminUserService.getUserById(id)));
    }

    /**
     * Deactivates the account of the specified user.
     *
     * <p>An admin cannot deactivate their own account.</p>
     *
     * @param id user UUID
     * @return success response
     */
    @PatchMapping("/users/{id}/deactivate")
    public ResponseEntity<ApiResponse<UserDto>> deactivateUser(@PathVariable String id) {
        adminUserService.deactivateUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deactivated successfully", adminUserService.getUserById(id)));
    }

    /**
     * Resets the password for the specified user.
     *
     * @param id   user UUID
     * @param body JSON object with a {@code newPassword} field
     * @return success response
     */
    @PostMapping("/users/{id}/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @PathVariable String id,
            @RequestBody Map<String, String> body
    ) {
        String newPassword = body.get("newPassword");
        if (newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("newPassword must not be blank"));
        }
        adminUserService.resetPassword(id, newPassword);
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully", null));
    }

    // ===========================================================================
    // System configuration
    // ===========================================================================

    /**
     * Returns all system configuration entries as a key-value map.
     *
     * @return map of config key → value
     */
    @GetMapping("/configs")
    public ResponseEntity<ApiResponse<Map<String, String>>> getAllConfigs() {
        Map<String, String> configs = systemConfigService.getAllConfigs();
        return ResponseEntity.ok(ApiResponse.success("Configurations retrieved successfully", configs));
    }

    /**
     * Creates or updates the configuration entry for the given key.
     *
     * @param key  the configuration key (path variable)
     * @param body JSON object with a {@code value} field
     * @return success response
     */
    @PutMapping("/configs/{key}")
    public ResponseEntity<ApiResponse<Void>> setConfig(
            @PathVariable String key,
            @RequestBody Map<String, String> body
    ) {
        String value = body.get("value");
        if (value == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Request body must contain a 'value' field"));
        }
        systemConfigService.setConfig(key, value);
        return ResponseEntity.ok(ApiResponse.success("Configuration saved successfully", null));
    }

    /**
     * Deletes the configuration entry for the given key.
     *
     * @param key the configuration key to remove
     * @return success response
     */
    @DeleteMapping("/configs/{key}")
    public ResponseEntity<ApiResponse<Void>> deleteConfig(@PathVariable String key) {
        systemConfigService.deleteConfig(key);
        return ResponseEntity.ok(ApiResponse.success("Configuration deleted successfully", null));
    }

    // ===========================================================================
    // Audit logs
    // ===========================================================================

    /**
     * Returns a paginated list of audit log entries, sorted by most recent first.
     *
     * @param pageable pagination parameters (default page size 50)
     * @return paged audit logs wrapped in ApiResponse
     */
    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLog>>> getAuditLogs(
            @PageableDefault(size = 50) Pageable pageable
    ) {
        Page<AuditLog> page = auditLogRepository.findAll(pageable);
        List<AuditLog> content = page.getContent();
        PagedResponse<AuditLog> paged = PagedResponse.<AuditLog>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
        return ResponseEntity.ok(ApiResponse.success("Audit logs retrieved successfully", paged));
    }
}
