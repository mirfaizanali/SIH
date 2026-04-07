package com.placement.portal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload returned after a successful login, registration, or token refresh.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    /** The JWT access token to be included in subsequent API requests. */
    private String accessToken;

    /** Always {@code "Bearer"}. */
    @Builder.Default
    private String tokenType = "Bearer";

    /** Number of milliseconds until the access token expires. */
    private long expiresIn;

    /** The authenticated user's role name (e.g. {@code "STUDENT"}). */
    private String role;

    /** The authenticated user's UUID identifier. */
    private String userId;

    /** The authenticated user's display name. */
    private String fullName;
}
