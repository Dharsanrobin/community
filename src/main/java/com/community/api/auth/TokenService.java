
package com.community.api.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class TokenService {

    @Value("${app.jwt.access-secret}")
    private String accessSecret;

    @Value("${app.jwt.refresh-secret}")
    private String refreshSecret;

    @Value("${app.jwt.issuer}")
    private String issuer;

    @Value("${app.jwt.audience}")
    private String audience;

    @Value("${app.jwt.access-expiration-ms}")
    private long accessExpirationMs;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    private final SecureRandom secureRandom = new SecureRandom();

    /*
     * Create the signing key for access tokens.
     * HS256 requires a key of at least 32 bytes.
     */
    private SecretKey accessKey() {
        byte[] key = accessSecret.getBytes(StandardCharsets.UTF_8);

        if (key.length < 32) {
            throw new IllegalStateException(
                    "JWT_ACCESS_SECRET must contain at least 32 bytes");
        }

        return Keys.hmacShaKeyFor(key);
    }

    /*
     * Create a signed JWT access token.
     * sessionId is a UUID string matching user_sessions.id (CHAR(36)).
     */
    public String createAccessToken(
            Long userId,
            String role,
            String sessionId,
            Integer tokenVersion) {

        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("role", role);
        claims.put("sid", sessionId);
        claims.put("tv", tokenVersion);

        long now = System.currentTimeMillis();

        return Jwts.builder()
                .claims(claims)
                .subject(String.valueOf(userId))
                .issuer(issuer)
                .audience()
                .add(audience)
                .and()
                .issuedAt(new Date(now))
                .expiration(new Date(now + accessExpirationMs))
                .signWith(accessKey(), Jwts.SIG.HS256)
                .compact();
    }

    /*
     * Verify the JWT signature, issuer, audience and expiration.
     */
    public Claims verifyAccessToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(accessKey())
                    .requireIssuer(issuer)
                    .requireAudience(audience)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

        } catch (JwtException | IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Invalid or expired access token", ex);
        }
    }

    /*
     * Generate a cryptographically secure opaque refresh token.
     */
    public String generateRefreshToken() {
        byte[] bytes = new byte[48];
        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    /*
     * Hash a refresh token with HMAC-SHA256.
     * Store the hash in the database, not the original token.
     */
    public String hashRefreshToken(String token) {
        byte[] keyBytes = refreshSecret.getBytes(StandardCharsets.UTF_8);

        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "JWT_REFRESH_SECRET must contain at least 32 bytes");
        }

        try {
            Mac mac = Mac.getInstance("HmacSHA256");

            SecretKey key = new SecretKeySpec(
                    keyBytes,
                    "HmacSHA256");

            mac.init(key);

            byte[] hash = mac.doFinal(
                    token.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);

        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Could not hash refresh token", ex);
        }
    }

    /*
     * Return the configured access-token lifetime in milliseconds.
     */
    public long getAccessExpirationMs() {
        return accessExpirationMs;
    }

    /*
     * Return the configured refresh-token lifetime in milliseconds.
     */
    public long getRefreshExpirationMs() {
        return refreshExpirationMs;
    }

    /*
     * Calculate the refresh-token expiration date.
     */
    public Date getRefreshExpirationDate() {
        return new Date(
                System.currentTimeMillis() + refreshExpirationMs);
    }
}
