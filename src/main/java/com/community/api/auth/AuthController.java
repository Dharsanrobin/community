
package com.community.api.auth;

import com.community.api.auth.dto.LoginRequest;
import com.community.api.auth.dto.LoginResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        String platform = httpRequest.getHeader("X-Platform");
        String deviceName = httpRequest.getHeader("X-Device-Name");
        String userAgent = httpRequest.getHeader("User-Agent");
        String ipAddress = httpRequest.getRemoteAddr();

        LoginResponse response = authService.login(
                request,
                platform,
                deviceName,
                userAgent,
                ipAddress);

        return ResponseEntity.ok(response);
    }
}
