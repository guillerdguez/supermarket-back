package com.supermarket.supermarket.service.security;

import com.supermarket.supermarket.dto.auth.AuthResponse;
import com.supermarket.supermarket.dto.auth.LoginRequest;
import com.supermarket.supermarket.dto.auth.RegisterRequest;
import com.supermarket.supermarket.dto.auth.UserResponse;
import com.supermarket.supermarket.exception.DuplicateResourceException;
import com.supermarket.supermarket.exception.RateLimitExceededException;
import com.supermarket.supermarket.model.audit.AuditStatus;
import com.supermarket.supermarket.model.user.User;
import com.supermarket.supermarket.model.user.UserRole;
import com.supermarket.supermarket.repository.UserRepository;
import com.supermarket.supermarket.security.SecurityUser;
import com.supermarket.supermarket.validator.PasswordValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RateLimitService rateLimitService;
    private final AuditService auditService;
    private final PasswordValidator passwordValidator;

    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getEmail());
        List<String> passwordErrors = passwordValidator.validatePassword(request.getPassword());
        if (!passwordErrors.isEmpty()) {
            auditService.logAction(request.getEmail(), "REGISTER_FAILED",
                    "Password validation failed", AuditStatus.FAILED);
            throw new IllegalArgumentException(
                    "Password validation failed: " + String.join(", ", passwordErrors)
            );
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            auditService.logAction(request.getEmail(), "REGISTER_FAILED",
                    "Email already exists", AuditStatus.FAILED);
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            auditService.logAction(request.getEmail(), "REGISTER_FAILED",
                    "Username already taken", AuditStatus.FAILED);
            throw new DuplicateResourceException("Username already taken: " + request.getUsername());
        }
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(UserRole.CASHIER)
                .active(true)
                .build();
        User savedUser = userRepository.save(user);
        SecurityUser securityUser = new SecurityUser(savedUser);
        String token = jwtService.generateToken(securityUser);
        auditService.logAction(savedUser.getEmail(), "REGISTER_SUCCESS",
                "New user registered", AuditStatus.SUCCESS);
        log.info("User registered successfully: {}", savedUser.getEmail());
        return AuthResponse.builder()
                .token(token)
                .user(convertToUserResponse(savedUser))
                .build();
    }

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
                .build();
    }
}