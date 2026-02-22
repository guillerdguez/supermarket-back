package com.supermarket.supermarket.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.supermarket.supermarket.controller.UserController;
import com.supermarket.supermarket.dto.auth.UserResponse;
import com.supermarket.supermarket.dto.user.RoleUpdateRequest;
import com.supermarket.supermarket.dto.user.UserRequest;
import com.supermarket.supermarket.exception.DuplicateResourceException;
import com.supermarket.supermarket.exception.GlobalExceptionHandler;
import com.supermarket.supermarket.exception.ResourceNotFoundException;
import com.supermarket.supermarket.model.UserRole;
import com.supermarket.supermarket.service.business.UserManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.doThrow;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;
    @Mock
    private UserManagementService userManagementService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        UserController userController = new UserController(userManagementService);
        PageableHandlerMethodArgumentResolver pageableResolver = new PageableHandlerMethodArgumentResolver();
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setCustomArgumentResolvers(pageableResolver)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private UserResponse buildUserResponse() {
        return UserResponse.builder()
                .id(1L).username("cashier1").email("cashier@test.com")
                .firstName("John").lastName("Doe").role("CASHIER").build();
    }

    @Test
    @DisplayName("GET /users - should return paginated list")
    void getAll_ShouldReturnPage() throws Exception {
        Page<UserResponse> page = new PageImpl<>(List.of(buildUserResponse()), PageRequest.of(0, 10), 1);
        given(userManagementService.getAll(any(), any(), any(), any())).willReturn(page);
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    @DisplayName("GET /users/{id} - should return user")
    void getById_ShouldReturnUser() throws Exception {
        given(userManagementService.getById(1L)).willReturn(buildUserResponse());
        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("cashier@test.com"));
    }

    @Test
    @DisplayName("GET /users/{id} - should return 404 when not found")
    void getById_WhenNotFound_ShouldReturn404() throws Exception {
        given(userManagementService.getById(999L)).willThrow(new ResourceNotFoundException("User not found"));
        mockMvc.perform(get("/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /users - should create user and return 201")
    void create_ShouldReturn201() throws Exception {
        UserRequest request = UserRequest.builder()
                .username("cashier1").email("cashier@test.com").password("Password1!")
                .firstName("John").lastName("Doe").role(UserRole.CASHIER).build();
        given(userManagementService.create(any())).willReturn(buildUserResponse());
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.email").value("cashier@test.com"));
    }

    @Test
    @DisplayName("POST /users - should return 400 when invalid request")
    void create_WithInvalidRequest_ShouldReturn400() throws Exception {
        UserRequest invalid = UserRequest.builder()
                .username("").email("notanemail").password("")
                .firstName("").lastName("").role(null).build();
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /users - should return 409 when email already exists")
    void create_WhenEmailExists_ShouldReturn409() throws Exception {
        UserRequest request = UserRequest.builder()
                .username("cashier1").email("cashier@test.com").password("Password1!")
                .firstName("John").lastName("Doe").role(UserRole.CASHIER).build();
        given(userManagementService.create(any())).willThrow(new DuplicateResourceException("Email already registered"));
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PUT /users/{id} - should update user")
    void update_ShouldReturnUpdatedUser() throws Exception {
        UserRequest request = UserRequest.builder()
                .username("cashier1").email("cashier@test.com").password("Password1!")
                .firstName("John").lastName("Doe").role(UserRole.CASHIER).build();
        given(userManagementService.update(eq(1L), any())).willReturn(buildUserResponse());
        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("cashier@test.com"));
    }

    @Test
    @DisplayName("PUT /users/{id}/role - should update role")
    void updateRole_ShouldReturnUpdatedUser() throws Exception {
        UserResponse updated = buildUserResponse();
        updated.setRole("MANAGER");
        given(userManagementService.updateRole(eq(1L), any())).willReturn(updated);
        mockMvc.perform(put("/users/1/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RoleUpdateRequest(UserRole.MANAGER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("MANAGER"));
    }

    @Test
    @DisplayName("PUT /users/{id}/role - should return 400 when role is null")
    void updateRole_WithNullRole_ShouldReturn400() throws Exception {
        mockMvc.perform(put("/users/1/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\": null}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /users/{id} - should return 204")
    void delete_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /users/{id} - should return 404 when not found")
    void delete_WhenNotFound_ShouldReturn404() throws Exception {
        doThrow(new ResourceNotFoundException("User not found")).when(userManagementService).delete(999L);
        mockMvc.perform(delete("/users/999"))
                .andExpect(status().isNotFound());
    }
}