package com.placement.portal.service.auth;

import com.placement.portal.domain.EmployerProfile;
import com.placement.portal.domain.FacultyProfile;
import com.placement.portal.domain.PlacementOfficerProfile;
import com.placement.portal.domain.StudentProfile;
import com.placement.portal.domain.User;
import com.placement.portal.domain.enums.Role;
import com.placement.portal.dto.request.LoginRequest;
import com.placement.portal.dto.request.RegisterRequest;
import com.placement.portal.dto.response.AuthResponse;
import com.placement.portal.repository.EmployerProfileRepository;
import com.placement.portal.repository.FacultyProfileRepository;
import com.placement.portal.repository.PlacementOfficerProfileRepository;
import com.placement.portal.repository.StudentProfileRepository;
import com.placement.portal.repository.UserRepository;
import com.placement.portal.security.jwt.JwtProperties;
import com.placement.portal.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles user authentication, registration, token refresh, and logout.
 *
 * <p>Access tokens are short-lived JWTs; refresh tokens are opaque UUIDs stored
 * as SHA-256 hashes. Token rotation is performed on every refresh call.</p>
 *
 * <p>Methods suffixed with {@code WithToken} return an {@link AuthResult} record
 * that bundles both the {@link AuthResponse} DTO and the raw (un-hashed) refresh
 * token string so the controller can set it as an httpOnly cookie.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    // -----------------------------------------------------------------------
    // Return type
    // -----------------------------------------------------------------------

    /**
     * Carries the JSON-serialisable {@link AuthResponse} alongside the raw refresh
     * token that must be placed in an httpOnly cookie by the controller.
     *
     * @param authResponse   the DTO returned to the client
     * @param rawRefreshToken the plaintext UUID refresh token to set as a cookie
     */
    public record AuthResult(AuthResponse authResponse, String rawRefreshToken) {}

    // -----------------------------------------------------------------------
    // Dependencies
    // -----------------------------------------------------------------------

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProperties jwtProperties;

    // Profile repositories for shell creation on registration
    private final StudentProfileRepository studentProfileRepository;
    private final EmployerProfileRepository employerProfileRepository;
    private final FacultyProfileRepository facultyProfileRepository;
    private final PlacementOfficerProfileRepository placementOfficerProfileRepository;

    // -----------------------------------------------------------------------
    // Login
    // -----------------------------------------------------------------------

    /**
     * Authenticates the user with their email and password.
     *
     * @param request validated login credentials
     * @return an {@link AuthResult} containing the access token DTO and raw refresh token
     */
    @Transactional
    public AuthResult loginWithToken(LoginRequest request) {
        // Let Spring Security validate credentials; throws BadCredentialsException on failure.
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated user not found in repository for email: " + request.getEmail()));

        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String rawRefreshToken = refreshTokenService.createRefreshToken(user);

        log.info("User logged in: userId={}, role={}", user.getId(), user.getRole());
        return new AuthResult(buildAuthResponse(user, accessToken), rawRefreshToken);
    }

    /**
     * Convenience delegation for backward-compatible callers that only need the
     * {@link AuthResponse} and manage the cookie themselves.
     *
     * @param request validated login credentials
     * @return the {@link AuthResponse} DTO (refresh token cookie must be set by caller)
     * @deprecated Prefer {@link #loginWithToken(LoginRequest)} to also obtain the raw token.
     */
    @Deprecated(forRemoval = true)
    @Transactional
    public AuthResponse login(LoginRequest request) {
        return loginWithToken(request).authResponse();
    }

    // -----------------------------------------------------------------------
    // Register
    // -----------------------------------------------------------------------

    /**
     * Registers a new user and creates their role-specific profile shell.
     *
     * @param request validated registration payload
     * @return an {@link AuthResult} containing the access token DTO and raw refresh token
     * @throws DataIntegrityViolationException if the email is already in use
     */
    @Transactional
    public AuthResult registerWithToken(RegisterRequest request) {
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

        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String rawRefreshToken = refreshTokenService.createRefreshToken(user);

        log.info("User registered: userId={}, role={}", user.getId(), user.getRole());
        return new AuthResult(buildAuthResponse(user, accessToken), rawRefreshToken);
    }

    /**
     * Convenience delegation — see {@link #loginWithToken(LoginRequest)}.
     *
     * @deprecated Prefer {@link #registerWithToken(RegisterRequest)}.
     */
    @Deprecated(forRemoval = true)
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        return registerWithToken(request).authResponse();
    }

    // -----------------------------------------------------------------------
    // Refresh
    // -----------------------------------------------------------------------

    /**
     * Validates the supplied raw refresh token, rotates it, and issues new tokens.
     *
     * @param rawRefreshToken the opaque UUID token from the client cookie
     * @return an {@link AuthResult} with a fresh access token and the new raw refresh token
     */
    @Transactional
    public AuthResult refreshWithToken(String rawRefreshToken) {
        // validateAndRotate revokes the old token and creates the next one internally;
        // we need to capture the new raw token from a second rotation pass.
        RefreshTokenService.RotationResult rotation =
                refreshTokenService.validateAndRotateWithNewToken(rawRefreshToken);

        String accessToken = jwtTokenProvider.generateAccessToken(rotation.user());

        log.debug("Token refreshed for userId={}", rotation.user().getId());
        return new AuthResult(buildAuthResponse(rotation.user(), accessToken), rotation.newRawToken());
    }

    /**
     * Convenience delegation — see {@link #refreshWithToken(String)}.
     *
     * @deprecated Prefer {@link #refreshWithToken(String)}.
     */
    @Deprecated(forRemoval = true)
    @Transactional
    public AuthResponse refresh(String rawRefreshToken) {
        return refreshWithToken(rawRefreshToken).authResponse();
    }

    // -----------------------------------------------------------------------
    // Logout
    // -----------------------------------------------------------------------

    /**
     * Revokes all refresh tokens for the given user.
     *
     * @param userId the UUID of the user to log out
     */
    @Transactional
    public void logout(String userId) {
        refreshTokenService.revokeAllUserTokens(userId);
        log.info("User logged out: userId={}", userId);
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private AuthResponse buildAuthResponse(User user, String accessToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(jwtProperties.getAccessTokenExpiryMs())
                .role(user.getRole().name())
                .userId(user.getId())
                .fullName(user.getFullName())
                .build();
    }

    /**
     * Creates an empty profile record for the user's role so that downstream
     * services always find a profile even before the user completes onboarding.
     */
    private void createProfileShell(User user) {
        Role role = user.getRole();
        switch (role) {
            case STUDENT -> {
                StudentProfile profile = StudentProfile.builder()
                        .userId(user.getId())
                        .user(user)
                        .build();
                studentProfileRepository.save(profile);
            }
            case EMPLOYER -> {
                EmployerProfile profile = EmployerProfile.builder()
                        .user(user)
                        .companyName("") // Will be populated during onboarding
                        .build();
                employerProfileRepository.save(profile);
            }
            case FACULTY_MENTOR -> {
                FacultyProfile profile = FacultyProfile.builder()
                        .user(user)
                        .build();
                facultyProfileRepository.save(profile);
            }
            case PLACEMENT_OFFICER -> {
                PlacementOfficerProfile profile = PlacementOfficerProfile.builder()
                        .user(user)
                        .build();
                placementOfficerProfileRepository.save(profile);
            }
            case ADMIN -> {
                // Admins have no dedicated profile entity.
            }
        }
    }
}
