package com.supermarket.supermarket.service.business;

import com.supermarket.supermarket.dto.auth.UserResponse;
import com.supermarket.supermarket.dto.user.ChangePasswordRequest;
import com.supermarket.supermarket.dto.user.ProfileUpdateRequest;
import com.supermarket.supermarket.dto.user.RoleUpdateRequest;
import com.supermarket.supermarket.dto.user.UserRequest;
import com.supermarket.supermarket.model.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserManagementService {
    Page<UserResponse> getAll(String username, String email, UserRole role, Pageable pageable);

    UserResponse getById(Long id);

    UserResponse create(UserRequest request);

    UserResponse update(Long id, UserRequest request);

    UserResponse updateRole(Long id, RoleUpdateRequest request);

    void delete(Long id);

    UserResponse getProfile();

    UserResponse updateProfile(ProfileUpdateRequest request);

    void changePassword(ChangePasswordRequest request);
}