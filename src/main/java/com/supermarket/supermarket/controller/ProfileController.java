package com.supermarket.supermarket.controller;

import com.supermarket.supermarket.dto.auth.UserResponse;
import com.supermarket.supermarket.dto.user.ChangePasswordRequest;
import com.supermarket.supermarket.dto.user.ProfileUpdateRequest;
import com.supermarket.supermarket.service.business.UserManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "Endpoints for current user profile management")
@SecurityRequirement(name = "Bearer Authentication")
public class ProfileController {

    private final UserManagementService userManagementService;

    @GetMapping
    @Operation(summary = "Get current user profile")
    public ResponseEntity<UserResponse> getProfile() {
        return ResponseEntity.ok(userManagementService.getProfile());
    }

    @PutMapping
    @Operation(summary = "Update current user profile")
    public ResponseEntity<UserResponse> updateProfile(@Valid @RequestBody ProfileUpdateRequest request) {
        return ResponseEntity.ok(userManagementService.updateProfile(request));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change current user password")
    public ResponseEntity<Map<String, String>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userManagementService.changePassword(request);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }
}