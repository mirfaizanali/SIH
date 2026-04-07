package com.placement.portal.controller;

import com.placement.portal.dto.request.LoginRequest;
import com.placement.portal.dto.request.RegisterRequest;
import com.placement.portal.dto.response.ApiResponse;
import com.placement.portal.dto.response.AuthResponse;
import com.placement.portal.security.jwt.JwtProperties;
import com.placement.portal.service.auth.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Optional;

/**
 * Authentication REST API.
 *
 * <table border="1">
 *   <caption>Endpoints</caption>
 *   <tr><th>Method</th><th>Path</th><th>Description</th></tr>
 *   <tr><td>POST</td><td>/api/auth/login</td><td>Authenticate and receive tokens</td></tr>
 *   <tr><td>POST</td><td>/api/auth/register</td><td>Create a new account</td></tr>
 *   <tr><td>POST</td><td>/api/auth/refresh</td><td>Rotate refresh token</td></tr>
 *   <tr><td>POST</td><td>/api/auth/logout</td><td>Revoke tokens and clear cookie</td></tr>
 * </table>
 *
 * <p>The refresh token is transported exclusively via an {@code httpOnly}, {@code SameSite=Strict}
 * cookie and is never returned in the JSON body.</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtProperties jwtProperties;

    // -----------------------------------------------------------------------
    // Login
    // -----------------------------------------------------------------------

    /**
     * Authenticates the user and issues an access token + refresh token cookie.
     *
     * @param request  validated login credentials
     * @param response the HTTP response used to set the refresh token cookie
     * @return 200 OK with {@link AuthResponse} body
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @RequestBody @Valid LoginRequest request,
            HttpServletResponse response
    ) {
        AuthService.AuthResult result = authService.loginWithToken(request);
        setRefreshTokenCookie(response, result.rawRefreshToken(),
                (int) (jwtProperties.getRefreshTokenExpiryMs() / 1_000));

        return ResponseEntity.ok(ApiResponse.success("Login successful", result.authResponse()));
    }

    // -----------------------------------------------------------------------
    // Register
    // -----------------------------------------------------------------------

    /**
     * Registers a new user account.
     *
     * @param request  validated registration payload
     * @param response the HTTP response used to set the refresh token cookie
     * @return 201 Created with {@link AuthResponse} body
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @RequestBody @Valid RegisterRequest request,
            HttpServletResponse response
    ) {
        AuthService.AuthResult result = authService.registerWithToken(request);
        setRefreshTokenCookie(response, result.rawRefreshToken(),
                (int) (jwtProperties.getRefreshTokenExpiryMs() / 1_000));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful", result.authResponse()));
    }

    // -----------------------------------------------------------------------
    // Refresh
    // -----------------------------------------------------------------------

    /**
     * Rotates the refresh token and issues a new access token.
     *
     * @param request  the HTTP request used to read the existing refresh token cookie
     * @param response the HTTP response used to set the new refresh token cookie
     * @return 200 OK with {@link AuthResponse} body, or 401 if cookie is missing/invalid
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String rawToken = extractRefreshTokenCookie(request).orElse(null);
        if (rawToken == null || rawToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Refresh token cookie is missing"));
        }

        AuthService.AuthResult result = authService.refreshWithToken(rawToken);
        setRefreshTokenCookie(response, result.rawRefreshToken(),
                (int) (jwtProperties.getRefreshTokenExpiryMs() / 1_000));

        return ResponseEntity.ok(ApiResponse.success("Token refreshed", result.authResponse()));
    }

    // -----------------------------------------------------------------------
    // Logout
    // -----------------------------------------------------------------------

    /**
     * Revokes all refresh tokens for the authenticated user and clears the cookie.
     *
     * @param response the HTTP response used to clear the refresh token cookie
     * @return 200 OK
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String userId = authentication.getName();
            authService.logout(userId);
        }

        clearRefreshTokenCookie(response);
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private void setRefreshTokenCookie(HttpServletResponse response, String rawToken, int maxAgeSeconds) {
        Cookie cookie = new Cookie(jwtProperties.getRefreshTokenCookieName(), rawToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // Set to false in local dev if not using HTTPS
        cookie.setPath("/api/auth");
        cookie.setMaxAge(maxAgeSeconds);
        // SameSite=Strict is set via response header because the Cookie API doesn't support it.
        response.addCookie(cookie);
        response.addHeader("Set-Cookie",
                buildSameSiteStrictHeader(jwtProperties.getRefreshTokenCookieName(),
                        rawToken, maxAgeSeconds));
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(jwtProperties.getRefreshTokenCookieName(), "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/api/auth");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private String buildSameSiteStrictHeader(String name, String value, int maxAge) {
        return String.format("%s=%s; Max-Age=%d; Path=/api/auth; HttpOnly; Secure; SameSite=Strict",
                name, value, maxAge);
    }

    private Optional<String> extractRefreshTokenCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        return Arrays.stream(cookies)
                .filter(c -> jwtProperties.getRefreshTokenCookieName().equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }
}
