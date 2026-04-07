package com.placement.portal.service.auth;

import com.placement.portal.domain.RefreshToken;
import com.placement.portal.domain.User;
import com.placement.portal.repository.RefreshTokenRepository;
import com.placement.portal.security.jwt.JwtProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

/**
 * Manages creation, validation, rotation, and revocation of refresh tokens.
 *
 * <p>The raw token (a UUID string) is <em>never</em> persisted; only its
 * SHA-256 hex digest is stored so that a database breach does not expose
 * valid tokens.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenService {

    // -----------------------------------------------------------------------
    // Return type for rotate operations
    // -----------------------------------------------------------------------

    /**
     * Carries both the owning {@link User} and the new raw refresh token produced
     * during a rotation so callers can forward both to the client.
     *
     * @param user        the token owner
     * @param newRawToken the newly generated raw UUID token (un-hashed)
     */
    public record RotationResult(User user, String newRawToken) {}

    // -----------------------------------------------------------------------
    // Dependencies
    // -----------------------------------------------------------------------

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Generates a new raw refresh token, persists its hash, and returns the raw value.
     *
     * @param user the owner of the token
     * @return the raw (un-hashed) UUID token string — must be sent to the client as a cookie
     */
    public String createRefreshToken(User user) {
        String rawToken = UUID.randomUUID().toString();
        String tokenHash = hashToken(rawToken);

        LocalDateTime expiresAt = LocalDateTime.now()
                .plusSeconds(jwtProperties.getRefreshTokenExpiryMs() / 1_000);

        RefreshToken entity = RefreshToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .expiresAt(expiresAt)
                .build();

        refreshTokenRepository.save(entity);
        log.debug("Created refresh token for userId={}", user.getId());
        return rawToken;
    }

    /**
     * Validates the supplied raw token, revokes it, issues a new one, and returns
     * both the owning {@link User} and the new raw token.
     *
     * <p>If a revoked token is presented (possible token reuse attack), all tokens
     * for the owning user are immediately revoked and an exception is thrown.</p>
     *
     * @param rawToken the raw token received from the client cookie
     * @return a {@link RotationResult} with the user and the new raw refresh token
     * @throws IllegalArgumentException if the token is unknown, expired, or already revoked
     */
    public RotationResult validateAndRotateWithNewToken(String rawToken) {
        String tokenHash = hashToken(rawToken);

        RefreshToken stored = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token not found"));

        if (stored.isRevoked()) {
            // Possible replay attack — revoke everything for this user as a safeguard.
            log.warn("Revoked refresh token reuse detected for userId={}. Revoking all tokens.",
                    stored.getUser().getId());
            revokeAllUserTokens(stored.getUser().getId());
            throw new IllegalArgumentException("Refresh token has been revoked");
        }

        if (stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Refresh token has expired");
        }

        // Revoke the consumed token.
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        User user = stored.getUser();
        // Issue the next token in the rotation chain.
        String newRawToken = createRefreshToken(user);

        log.debug("Refresh token rotated for userId={}", user.getId());
        return new RotationResult(user, newRawToken);
    }

    /**
     * Validates the supplied raw token and returns only the owning {@link User}.
     * <p>
     * Use {@link #validateAndRotateWithNewToken(String)} when you also need the
     * new raw token (e.g. from the controller so it can set the cookie).
     * </p>
     *
     * @param rawToken the raw token received from the client cookie
     * @return the {@link User} associated with the valid token
     * @throws IllegalArgumentException if the token is unknown, expired, or already revoked
     */
    public User validateAndRotate(String rawToken) {
        return validateAndRotateWithNewToken(rawToken).user();
    }

    /**
     * Marks all refresh tokens for the given user as revoked.
     * <p>
     * Called on logout and on token-reuse detection.
     * </p>
     *
     * @param userId the user's UUID string
     */
    public void revokeAllUserTokens(String userId) {
        List<RefreshToken> tokens = refreshTokenRepository.findAll().stream()
                .filter(t -> t.getUser().getId().equals(userId) && !t.isRevoked())
                .toList();

        tokens.forEach(t -> t.setRevoked(true));
        refreshTokenRepository.saveAll(tokens);

        log.debug("Revoked {} refresh token(s) for userId={}", tokens.size(), userId);
    }

    // -----------------------------------------------------------------------
    // Helper
    // -----------------------------------------------------------------------

    /**
     * Computes the SHA-256 hex digest of {@code rawToken}.
     *
     * @param rawToken the plaintext token
     * @return lowercase hex string of the SHA-256 digest
     */
    public String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is guaranteed by the JDK specification — this can never happen.
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
