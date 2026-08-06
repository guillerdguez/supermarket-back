package com.supermarket.supermarket.service.security;

import com.supermarket.supermarket.dto.auth.AuthResponse;
import com.supermarket.supermarket.dto.auth.LoginRequest;
import com.supermarket.supermarket.dto.auth.UserResponse;
import com.supermarket.supermarket.exception.RateLimitExceededException;
import com.supermarket.supermarket.model.audit.AuditStatus;
import com.supermarket.supermarket.model.user.User;
import com.supermarket.supermarket.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthService {
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RateLimitService rateLimitService;
    private final AuditService auditService;

    public AuthResponse login(LoginRequest request) {
        String rateLimitKey = "login:" + request.getEmail();
        rateLimitService.checkRateLimit(rateLimitKey);
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
            SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
            User user = securityUser.getUser();
            String token = jwtService.generateToken(securityUser);
            rateLimitService.resetRateLimit(rateLimitKey);
            auditService.logAction(user.getEmail(), "LOGIN_SUCCESS",
                    "User logged in successfully", AuditStatus.SUCCESS);
            log.info("User logged in successfully: {}", user.getEmail());
            return AuthResponse.builder()
                    .token(token)
                    .user(convertToUserResponse(user))
                    .build();
        } catch (RateLimitExceededException e) {
            auditService.logAction(request.getEmail(), "LOGIN_FAILED",
                    "Rate limit exceeded", AuditStatus.FAILED);
            throw e;
        } catch (Exception e) {
            auditService.logAction(request.getEmail(), "LOGIN_FAILED",
                    "Invalid credentials", AuditStatus.FAILED);
            throw e;
        }
    }

    private UserResponse convertToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().name())
                .branchId(user.getBranch() != null ? user.getBranch().getId() : null)
                .branchName(user.getBranch() != null ? user.getBranch().getName() : null)
                .build();
    }
}