package com.placement.portal.security.jwt;

import com.placement.portal.domain.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

/**
 * Generates and validates JWT access tokens, and produces raw UUID refresh tokens.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

    // -----------------------------------------------------------------------
    // Token generation
    // -----------------------------------------------------------------------

    /**
     * Creates a signed HMAC-SHA512 access token for the given user.
     *
     * @param user the authenticated user entity
     * @return compact, signed JWT string
     */
    public String generateAccessToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getAccessTokenExpiryMs());

        return Jwts.builder()
                .subject(user.getId())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey(), Jwts.SIG.HS512)
                .compact();
    }

    /**
     * Returns a cryptographically random UUID string to be used as the raw refresh token.
     * <p>
     * The caller is responsible for SHA-256 hashing the value before persisting it.
     * </p>
     *
     * @return raw, un-hashed UUID refresh token
     */
    public String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }

    // -----------------------------------------------------------------------
    // Token validation / extraction
    // -----------------------------------------------------------------------

    /**
     * Validates the token's signature and expiry.
     *
     * @param token compact JWT string
     * @return {@code true} if the token is valid; {@code false} on any error
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException ex) {
            log.debug("JWT validation failed: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.debug("JWT token is null or empty: {}", ex.getMessage());
        }
        return false;
    }

    /**
     * Extracts the subject (user id) claim from a validated token.
     *
     * @param token compact JWT string (must already be validated)
     * @return the subject claim value (user UUID string)
     */
    public String getUserIdFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Extracts the {@code role} claim from a validated token.
     *
     * @param token compact JWT string (must already be validated)
     * @return the role name string (e.g. {@code "ADMIN"})
     */
    public String getRoleFromToken(String token) {
        return parseClaims(token).get("role", String.class);
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
