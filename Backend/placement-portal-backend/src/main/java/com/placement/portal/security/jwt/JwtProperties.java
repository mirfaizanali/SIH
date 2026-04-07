package com.placement.portal.security.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
@Getter
@Setter
public class JwtProperties {

    /**
     * Base64-encoded HMAC-SHA512 secret key (at least 64 bytes after decoding).
     */
    private String secret;

    /**
     * Access token expiry in milliseconds (e.g. 900000 = 15 minutes).
     */
    private long accessTokenExpiryMs = 900_000L;

    /**
     * Refresh token expiry in milliseconds (e.g. 604800000 = 7 days).
     */
    private long refreshTokenExpiryMs = 604_800_000L;

    /**
     * Name of the HTTP-only cookie that carries the refresh token.
     */
    private String refreshTokenCookieName = "refreshToken";
}
