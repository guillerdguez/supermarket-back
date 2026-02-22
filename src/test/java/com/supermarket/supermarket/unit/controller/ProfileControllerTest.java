package com.supermarket.supermarket.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.supermarket.supermarket.controller.ProfileController;
import com.supermarket.supermarket.dto.auth.UserResponse;
import com.supermarket.supermarket.dto.user.ChangePasswordRequest;
import com.supermarket.supermarket.dto.user.ProfileUpdateRequest;
import com.supermarket.supermarket.exception.GlobalExceptionHandler;
import com.supermarket.supermarket.exception.InvalidOperationException;
import com.supermarket.supermarket.service.business.UserManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.doThrow;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProfileControllerTest {

    private MockMvc mockMvc;
    @Mock
    private UserManagementService userManagementService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        ProfileController profileController = new ProfileController(userManagementService);
        mockMvc = MockMvcBuilders.standaloneSetup(profileController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private UserResponse buildUserResponse() {
        return UserResponse.builder()
                .id(1L).username("admin-test").email("admin@test.com")
                .firstName("Admin").lastName("System").role("ADMIN").build();
    }

    @Test
    @DisplayName("GET /profile - should return current user profile")
    void getProfile_ShouldReturnProfile() throws Exception {
        given(userManagementService.getProfile()).willReturn(buildUserResponse());
        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("admin@test.com"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @DisplayName("PUT /profile - should update profile")
    void updateProfile_ShouldReturnUpdatedProfile() throws Exception {
        ProfileUpdateRequest request = new ProfileUpdateRequest("newusername", "NewFirst", "NewLast");
        UserResponse updated = buildUserResponse();
        updated.setUsername("newusername");
        given(userManagementService.updateProfile(any())).willReturn(updated);
        mockMvc.perform(put("/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newusername"));
    }

    @Test
    @DisplayName("PUT /profile - should return 400 when request is invalid")
    void updateProfile_WithInvalidRequest_ShouldReturn400() throws Exception {
        ProfileUpdateRequest invalid = new ProfileUpdateRequest("", "", "");
        mockMvc.perform(put("/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /profile/change-password - should return 200 on success")
    void changePassword_ShouldReturn200() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("OldPass1!", "NewPass1!");
        mockMvc.perform(post("/profile/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password changed successfully"));
    }

    @Test
    @DisplayName("POST /profile/change-password - should return 400 when current password is wrong")
    void changePassword_WhenWrongCurrentPassword_ShouldReturn400() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("WrongPass!", "NewPass1!");
        doThrow(new InvalidOperationException("Current password is incorrect"))
                .when(userManagementService).changePassword(any());
        mockMvc.perform(post("/profile/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /profile/change-password - should return 400 when fields are blank")
    void changePassword_WithBlankFields_ShouldReturn400() throws Exception {
        ChangePasswordRequest invalid = new ChangePasswordRequest("", "");
        mockMvc.perform(post("/profile/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }
}