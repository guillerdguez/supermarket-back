package com.supermarket.supermarket.service.business.impl;

import com.supermarket.supermarket.dto.auth.UserResponse;
import com.supermarket.supermarket.dto.user.ChangePasswordRequest;
import com.supermarket.supermarket.dto.user.ProfileUpdateRequest;
import com.supermarket.supermarket.dto.user.RoleUpdateRequest;
import com.supermarket.supermarket.dto.user.UserRequest;
import com.supermarket.supermarket.exception.DuplicateResourceException;
import com.supermarket.supermarket.exception.InvalidOperationException;
import com.supermarket.supermarket.exception.ResourceNotFoundException;
import com.supermarket.supermarket.model.User;
import com.supermarket.supermarket.model.UserRole;
import com.supermarket.supermarket.repository.UserRepository;
import com.supermarket.supermarket.security.SecurityUtils;
import com.supermarket.supermarket.service.business.UserManagementService;
import com.supermarket.supermarket.specification.UserSpecifications;
import com.supermarket.supermarket.validator.PasswordValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserManagementServiceImpl implements UserManagementService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordValidator passwordValidator;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAll(String username, String email, UserRole role, Pageable pageable) {
        log.info("Fetching all users with filters");
        Specification<User> spec = UserSpecifications.withFilters(username, email, role);
        return userRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        log.info("Fetching user with ID: {}", id);
        return toResponse(findUser(id));
    }

    @Override
    public UserResponse create(UserRequest request) {
        log.info("Creating new user: {}", request.getEmail());
        List<String> passwordErrors = passwordValidator.validatePassword(request.getPassword());
        if (!passwordErrors.isEmpty()) {
            throw new IllegalArgumentException("Password validation failed: " + String.join(", ", passwordErrors));
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already taken: " + request.getUsername());
        }
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(request.getRole())
                .active(true)
                .build();
        User saved = userRepository.save(user);
        log.info("User created successfully: {}", saved.getEmail());
        return toResponse(saved);
    }

    @Override
    public UserResponse update(Long id, UserRequest request) {
        log.info("Updating user with ID: {}", id);
        User user = findUser(id);
        if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }
        if (!user.getUsername().equals(request.getUsername()) && userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already taken: " + request.getUsername());
        }
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRole(request.getRole());
        return toResponse(userRepository.save(user));
    }

    @Override
    public UserResponse updateRole(Long id, RoleUpdateRequest request) {
        log.info("Updating role for user ID: {} to {}", id, request.getRole());
        User user = findUser(id);
        user.setRole(request.getRole());
        return toResponse(userRepository.save(user));
    }

    @Override
    public void delete(Long id) {
        log.info("Deactivating user with ID: {}", id);
        User user = findUser(id);
        user.setActive(false);
        userRepository.save(user);
        log.info("User deactivated successfully - ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getProfile() {
        return toResponse(getCurrentUser());
    }

    @Override
    public UserResponse updateProfile(ProfileUpdateRequest request) {
        log.info("Updating profile for current user");
        User user = getCurrentUser();
        if (!user.getUsername().equals(request.getUsername()) && userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already taken: " + request.getUsername());
        }
        user.setUsername(request.getUsername());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        return toResponse(userRepository.save(user));
    }

    @Override
    public void changePassword(ChangePasswordRequest request) {
        log.info("Changing password for current user");
        User user = getCurrentUser();
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidOperationException("Current password is incorrect");
        }
        List<String> passwordErrors = passwordValidator.validatePassword(request.getNewPassword());
        if (!passwordErrors.isEmpty()) {
            throw new IllegalArgumentException("Password validation failed: " + String.join(", ", passwordErrors));
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed successfully for user: {}", user.getEmail());
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
    }

    private User getCurrentUser() {
        return securityUtils.getCurrentUser();
    }

    private UserResponse toResponse(User user) {
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