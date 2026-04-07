package com.placement.portal.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Utility methods for inspecting the current security context.
 *
 * <p>These helpers centralise all {@link SecurityContextHolder} access so that
 * service and controller classes do not need to repeat the boilerplate.</p>
 */
@Slf4j
@Component
public class SecurityUtils {

    // -----------------------------------------------------------------------
    // Current user identity
    // -----------------------------------------------------------------------

    /**
     * Returns the UUID of the currently authenticated user.
     *
     * @return the user's UUID string, or {@code null} if no authentication is present
     */
    public String getCurrentUserId() {
        Authentication auth = getAuthentication().orElse(null);
        if (auth == null) {
            return null;
        }
        return auth.getName();
    }

    /**
     * Returns the first role authority name of the currently authenticated user,
     * stripped of the {@code ROLE_} prefix.
     *
     * <p>Example: for authority {@code "ROLE_ADMIN"} this returns {@code "ADMIN"}.</p>
     *
     * @return the role name, or {@code null} if unauthenticated or no authority found
     */
    public String getCurrentUserRole() {
        Authentication auth = getAuthentication().orElse(null);
        if (auth == null || auth.getAuthorities() == null) {
            return null;
        }

        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .map(a -> a.startsWith("ROLE_") ? a.substring(5) : a)
                .orElse(null);
    }

    // -----------------------------------------------------------------------
    // Comparison helpers
    // -----------------------------------------------------------------------

    /**
     * Returns {@code true} if the currently authenticated user's id matches the
     * supplied {@code userId}.
     *
     * @param userId the user id to compare against
     * @return {@code true} if the ids match; {@code false} otherwise
     */
    public boolean isCurrentUser(String userId) {
        if (userId == null) {
            return false;
        }
        return userId.equals(getCurrentUserId());
    }

    /**
     * Returns {@code true} if the currently authenticated user holds the given role.
     *
     * <p>The {@code role} parameter may be supplied with or without the {@code ROLE_} prefix.</p>
     *
     * @param role the role name to check (e.g. {@code "ADMIN"} or {@code "ROLE_ADMIN"})
     * @return {@code true} if the user has the role; {@code false} otherwise
     */
    public boolean hasRole(String role) {
        Authentication auth = getAuthentication().orElse(null);
        if (auth == null || auth.getAuthorities() == null || role == null) {
            return false;
        }

        String normalised = role.startsWith("ROLE_") ? role : "ROLE_" + role;

        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(normalised::equals);
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private Optional<Authentication> getAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            return Optional.empty();
        }
        return Optional.of(auth);
    }
}
