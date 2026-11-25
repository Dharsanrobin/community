
package com.community.api.auth;

import com.community.api.auth.dto.LoginRequest;
import com.community.api.auth.dto.LoginResponse;
import com.community.api.user.User;
import com.community.api.user.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserSessionRepository userSessionRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            UserSessionRepository userSessionRepository,
            RefreshTokenRepository refreshTokenRepository,
            TokenService tokenService,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userSessionRepository = userSessionRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public LoginResponse login(
            LoginRequest request,
            String platform,
            String deviceName,
            String userAgent,
            String ipAddress) {

        log.info("Login request received");

        // 1. Validate request
        if (request == null
                || request.getIdentifier() == null
                || request.getIdentifier().isBlank()) {

            throw new BadCredentialsException(
                    "Invalid email/username or password");
        }

        if (request.getPassword() == null
                || request.getPassword().isEmpty()) {

            throw new BadCredentialsException(
                    "Invalid email/username or password");
        }

        // 2. Normalize identifier
        String identifier = request.getIdentifier()
                .trim()
                .toLowerCase(Locale.ROOT);

        // 3. Find user by email or username
        User user = userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByUsername(identifier))
                .orElseThrow(() -> new BadCredentialsException(
                        "Invalid email/username or password"));

        log.info("User found. User ID: {}", user.getId());

        // 4. Verify password once
        log.info("Checking password");

        if (user.getPasswordHash() == null
                || !passwordEncoder.matches(
                        request.getPassword(),
                        user.getPasswordHash())) {

            log.warn(
                    "Password verification failed for user ID: {}",
                    user.getId());

            throw new BadCredentialsException(
                    "Invalid email/username or password");
        }

        log.info("Password verification passed");

        // 5. Check account status
        if (user.getDeletedAt() != null) {
            throw new BadCredentialsException(
                    "Account is unavailable");
        }

        if ("SUSPENDED".equals(user.getStatus())
                || "BANNED".equals(user.getStatus())) {

            throw new BadCredentialsException(
                    "Account is unavailable");
        }

        if (!"ACTIVE".equals(user.getStatus())) {
            throw new BadCredentialsException(
                    "Account is not active");
        }

        if (user.getEmailVerifiedAt() == null) {
            throw new BadCredentialsException(
                    "Please verify your email before logging in");
        }

        log.info("Account status checks passed");

        // 6. Load user's role
        Role role = roleRepository.findById(user.getRoleId())
                .orElseThrow(() -> new IllegalStateException(
                        "The user's role was not found"));

        log.info("Role lookup passed");

        // 7. Normalize platform
        String normalizedPlatform = platform == null || platform.isBlank()
                ? "WEB"
                : platform.trim().toUpperCase(Locale.ROOT);

        if (!normalizedPlatform.matches(
                "WEB|ADMIN|ANDROID|IOS|OTHER")) {

            normalizedPlatform = "OTHER";
        }

        // 8. Create user session
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        String sessionId = UUID.randomUUID().toString();

        UserSession session = new UserSession();
        session.setId(sessionId);
        session.setUserId(user.getId());
        session.setPlatform(normalizedPlatform);
        session.setDeviceName(deviceName);
        session.setUserAgent(userAgent);
        session.setIpAddress(ipAddress);
        session.setLastUsedAt(now);
        session.setExpiresAt(
                now.plusSeconds(
                        tokenService.getRefreshExpirationMs() / 1000));

        log.info("Saving user session");

        userSessionRepository.saveAndFlush(session);

        log.info("User session saved");

        // 9. Generate and save refresh token
        String refreshToken = tokenService.generateRefreshToken();

        String refreshTokenHash = tokenService.hashRefreshToken(refreshToken);

        RefreshToken refreshTokenEntity = new RefreshToken();
        refreshTokenEntity.setSessionId(sessionId);
        refreshTokenEntity.setUserId(user.getId());
        refreshTokenEntity.setTokenHash(refreshTokenHash);
        refreshTokenEntity.setExpiresAt(
                now.plusSeconds(
                        tokenService.getRefreshExpirationMs() / 1000));

        log.info("Saving refresh token");

        refreshTokenRepository.saveAndFlush(refreshTokenEntity);

        log.info("Refresh token saved");

        // 10. Update last login
        user.setLastLoginAt(now);
        userRepository.saveAndFlush(user);

        log.info("Last login updated");

        // 11. Generate access token
        String accessToken = tokenService.createAccessToken(
                user.getId(),
                role.getName(),
                sessionId,
                user.getTokenVersion());

        log.info("Access token created successfully");

        // 12. Build user information
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
                user.getId(),
                user.getUuid(),
                user.getName(),
                user.getUsername(),
                user.getEmail(),
                role.getName());

        log.info("Login completed successfully");

        // 13. Return response
        return new LoginResponse(
                accessToken,
                refreshToken,
                tokenService.getAccessExpirationMs(),
                userInfo);
    }
}
