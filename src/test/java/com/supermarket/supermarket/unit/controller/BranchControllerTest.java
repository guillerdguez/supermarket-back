package com.supermarket.supermarket.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.supermarket.supermarket.controller.BranchController;
import com.supermarket.supermarket.dto.branch.BranchResponse;
import com.supermarket.supermarket.exception.GlobalExceptionHandler;
import com.supermarket.supermarket.exception.ResourceNotFoundException;
import com.supermarket.supermarket.service.business.BranchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static com.supermarket.supermarket.fixtures.branch.BranchFixtures.*;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class BranchControllerTest {
    private MockMvc mockMvc;
    @Mock
    private BranchService branchService;
    private ObjectMapper objectMapper;
    private BranchController branchController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        branchController = new BranchController(branchService);
        mockMvc = MockMvcBuilders.standaloneSetup(branchController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /branches - should return list")
    void getAll_ShouldReturnList() throws Exception {
        given(branchService.getAll()).willReturn(List.of(branchResponse()));
        mockMvc.perform(get("/branches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("GET /branches/{id} - should return branch")
    void getById_ShouldReturnBranch() throws Exception {
        given(branchService.getById(1L)).willReturn(branchResponse());
        mockMvc.perform(get("/branches/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Central Branch"));
    }

    @Test
    @DisplayName("GET /branches/{id} - should return 404 when not found")
    void getById_WhenNotFound_ShouldReturn404() throws Exception {
        given(branchService.getById(999L))
                .willThrow(new ResourceNotFoundException("Branch not found"));
        mockMvc.perform(get("/branches/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /branches - should create branch")
    void create_ShouldReturn201() throws Exception {
        BranchResponse response = branchResponse();
        given(branchService.create(any())).willReturn(response);
        mockMvc.perform(post("/branches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validBranchRequest())))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.name").value("Central Branch"));
    }

    @Test
    @DisplayName("POST /branches - should return 400 when invalid request")
    void create_WithInvalidRequest_ShouldReturn400() throws Exception {
        mockMvc.perform(post("/branches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidBranchRequest())))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /branches/{id} - should update branch")
    void update_ShouldReturnUpdatedBranch() throws Exception {
        BranchResponse response = branchResponse();
        given(branchService.update(eq(1L), any())).willReturn(response);
        mockMvc.perform(put("/branches/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validBranchRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Central Branch"));
    }

    @Test
    @DisplayName("DELETE /branches/{id} - should return 204")
    void delete_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/branches/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /branches/{id} - should return 404 when not found")
    void delete_WhenNotFound_ShouldReturn404() throws Exception {
        doThrow(new ResourceNotFoundException("Branch not found"))
                .when(branchService).delete(999L);
        mockMvc.perform(delete("/branches/999"))
                .andExpect(status().isNotFound());
    }
}