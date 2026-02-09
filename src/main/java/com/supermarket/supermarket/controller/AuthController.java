package com.supermarket.supermarket.controller;

import com.supermarket.supermarket.dto.auth.AuthResponse;
import com.supermarket.supermarket.dto.auth.LoginRequest;
import com.supermarket.supermarket.dto.auth.RegisterRequest;
import com.supermarket.supermarket.model.AuditStatus;
import com.supermarket.supermarket.service.security.AuditService;
import com.supermarket.supermarket.service.security.AuthService;
import com.supermarket.supermarket.service.security.JwtService;
import com.supermarket.supermarket.service.security.TokenBlacklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user authentication")
public class AuthController {

    private final AuthService authService;
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtService jwtService;
    private final AuditService auditService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email and password")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout and invalidate token")
    public ResponseEntity<Map<String, String>> logout(
            @RequestHeader("Authorization") String authHeader,
            HttpServletRequest request) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "No token provided"));
        }
        String token = authHeader.substring(7);
        try {
            String username = jwtService.getUsername(token);
            Date expiration = jwtService.getExpirationDate(token);
            tokenBlacklistService.blacklistToken(
                    token,
                    expiration.toInstant()
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDateTime()
            );
            auditService.logAction(username, "LOGOUT",
                    "User logged out from IP: " + getClientIp(request), AuditStatus.SUCCESS);
            return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid token"));
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String forwardedForHeader = request.getHeader("X-Forwarded-For");
        if (forwardedForHeader != null) {
            return forwardedForHeader.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}