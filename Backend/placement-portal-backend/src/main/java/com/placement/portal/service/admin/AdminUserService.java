package com.placement.portal.service.admin;

import com.placement.portal.domain.EmployerProfile;
import com.placement.portal.domain.FacultyProfile;
import com.placement.portal.domain.PlacementOfficerProfile;
import com.placement.portal.domain.StudentProfile;
import com.placement.portal.domain.User;
import com.placement.portal.domain.enums.Role;
import com.placement.portal.dto.request.RegisterRequest;
import com.placement.portal.dto.response.UserDto;
import com.placement.portal.exception.EntityNotFoundException;
import com.placement.portal.mapper.EntityMapper;
import com.placement.portal.repository.EmployerProfileRepository;
import com.placement.portal.repository.FacultyProfileRepository;
import com.placement.portal.repository.PlacementOfficerProfileRepository;
import com.placement.portal.repository.StudentProfileRepository;
import com.placement.portal.repository.UserRepository;
import com.placement.portal.util.SecurityUtils;
import org.springframework.dao.DataIntegrityViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Administrative operations on user accounts.
 *
 * <p>This service is responsible for:</p>
 * <ul>
 *   <li>Listing / searching users with optional role and active-status filters</li>
 *   <li>Activating and deactivating accounts</li>
 *   <li>Resetting passwords on behalf of users</li>
 * </ul>
 *
 * <p>All mutating methods require the caller to hold the {@code ADMIN} role
 * (enforced at the controller layer via {@code @PreAuthorize}).</p>
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository                   userRepository;
    private final StudentProfileRepository         studentProfileRepository;
    private final FacultyProfileRepository         facultyProfileRepository;
    private final EmployerProfileRepository        employerProfileRepository;
    private final PlacementOfficerProfileRepository placementOfficerProfileRepository;
    private final PasswordEncoder                  passwordEncoder;
    private final EntityMapper                     entityMapper;
    private final SecurityUtils                    securityUtils;

    // ---------------------------------------------------------------------------
    // Read operations
    // ---------------------------------------------------------------------------

    /**
     * Returns a paginated list of users, optionally filtered by role and/or active status.
     *
     * @param role     optional role name filter (e.g. "STUDENT", "EMPLOYER")
     * @param isActive optional active-status filter
     * @param pageable pagination and sorting parameters
     * @return page of UserDtos
     */
    @Transactional(readOnly = true)
    public Page<UserDto> getAllUsers(String role, Boolean isActive, Pageable pageable) {
        Page<User> users;

        if (role != null && isActive != null) {
            Role roleEnum = Role.valueOf(role.toUpperCase());
            users = userRepository.findByRoleAndIsActive(roleEnum, isActive, pageable);
        } else if (role != null) {
            Role roleEnum = Role.valueOf(role.toUpperCase());
            users = userRepository.findByRole(roleEnum, pageable);
        } else if (isActive != null) {
            users = userRepository.findByIsActive(isActive, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }

        return users.map(entityMapper::toUserDto);
    }

    /**
     * Returns the UserDto for the user with the given ID.
     *
     * @param id user UUID
     * @return UserDto
     * @throws EntityNotFoundException if no user with the given ID exists
     */
    @Transactional(readOnly = true)
    public UserDto getUserById(String id) {
        User user = loadUserById(id);
        return entityMapper.toUserDto(user);
    }

    // ---------------------------------------------------------------------------
    // Account lifecycle
    // ---------------------------------------------------------------------------

    /**
     * Activates the account of the user with the given ID.
     *
     * @param id user UUID to activate
     * @throws EntityNotFoundException if no user with the given ID exists
     */
    public void activateUser(String id) {
        User user = loadUserById(id);
        user.setActive(true);
        userRepository.save(user);
        log.info("Admin activated user: id={} email={}", user.getId(), user.getEmail());
    }

    /**
     * Deactivates the account of the user with the given ID.
     *
     * <p>An admin cannot deactivate their own account via this endpoint —
     * attempting to do so will throw an {@link IllegalStateException}.</p>
     *
     * @param id user UUID to deactivate
     * @throws EntityNotFoundException if no user with the given ID exists
     * @throws IllegalStateException   if the admin tries to deactivate their own account
     */
    public void deactivateUser(String id) {
        String currentUserId = securityUtils.getCurrentUserId();
        if (id.equals(currentUserId)) {
            throw new IllegalStateException("Administrators cannot deactivate their own account");
        }

        User user = loadUserById(id);
        user.setActive(false);
        userRepository.save(user);
        log.info("Admin deactivated user: id={} email={}", user.getId(), user.getEmail());
    }

    // ---------------------------------------------------------------------------
    // Password management
    // ---------------------------------------------------------------------------

    /**
     * Resets the password for the specified user to the provided new password.
     *
     * <p>The new password is BCrypt-hashed before persistence.</p>
     *
     * @param userId      the user's UUID
     * @param newPassword plain-text new password (will be hashed)
     * @throws EntityNotFoundException if no user with the given ID exists
     */
    public void resetPassword(String userId, String newPassword) {
        User user = loadUserById(userId);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Admin reset password for user: id={}", userId);
    }

    // ---------------------------------------------------------------------------
    // User creation
    // ---------------------------------------------------------------------------

    /**
     * Creates a new user account with a role-specific profile shell.
     * Unlike self-registration, no tokens are issued — the admin simply provisions
     * the account.
     *
     * @param request validated registration payload
     * @return UserDto of the newly created user
     * @throws DataIntegrityViolationException if the email is already taken
     */
    public UserDto createUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DataIntegrityViolationException(
                    "An account with email '" + request.getEmail() + "' already exists");
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(request.getRole())
                .isActive(true)
                .build();

        userRepository.save(user);
        createProfileShell(user);

        log.info("Admin created user: userId={}, role={}", user.getId(), user.getRole());
        return entityMapper.toUserDto(user);
    }

    // ---------------------------------------------------------------------------
    // Private helpers
    // ---------------------------------------------------------------------------

    private void createProfileShell(User user) {
        switch (user.getRole()) {
            case STUDENT -> studentProfileRepository.save(
                    StudentProfile.builder().userId(user.getId()).user(user).build());
            case EMPLOYER -> employerProfileRepository.save(
                    EmployerProfile.builder().user(user).companyName("").build());
            case FACULTY_MENTOR -> facultyProfileRepository.save(
                    FacultyProfile.builder().user(user).build());
            case PLACEMENT_OFFICER -> placementOfficerProfileRepository.save(
                    PlacementOfficerProfile.builder().user(user).build());
            case ADMIN -> { /* no profile entity for admins */ }
        }
    }

    private User loadUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + id));
    }
}
