package com.supermercado.supermercado.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supermercado.supermercado.controller.BranchController;
import com.supermercado.supermercado.dto.sucursalDto.SucursalRequest;
import com.supermercado.supermercado.dto.sucursalDto.SucursalResponse;
import com.supermercado.supermercado.exception.ResourceNotFoundException;
import com.supermercado.supermercado.fixtures.TestFixtures;
import com.supermercado.supermercado.service.SucursalService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BranchController.class)
class BranchControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private SucursalService branchService;

    @Test
    @DisplayName("GET /branches - should return list")
    void getAll_ShouldReturnList() throws Exception {
        given(branchService.getAll()).willReturn(List.of(TestFixtures.branchResponse()));

        mockMvc.perform(get("/branches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("GET /branches/{id} - should return branch")
    void getById_ShouldReturnBranch() throws Exception {
        given(branchService.getById(1L)).willReturn(TestFixtures.branchResponse());

        mockMvc.perform(get("/branches/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Central Branch"));
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
        SucursalResponse response = TestFixtures.branchResponse();
        given(branchService.create(any(SucursalRequest.class))).willReturn(response);

        mockMvc.perform(post("/branches")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(TestFixtures.validBranchRequest())))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.nombre").value("Central Branch"));
    }

    @Test
    @DisplayName("POST /branches - should return 400 when invalid request")
    void create_WithInvalidRequest_ShouldReturn400() throws Exception {
        mockMvc.perform(post("/branches")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(TestFixtures.invalidBranchRequest())))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /branches/{id} - should update branch")
    void update_ShouldReturnUpdatedBranch() throws Exception {
        SucursalResponse response = TestFixtures.branchResponse();
        given(branchService.update(eq(1L), any(SucursalRequest.class))).willReturn(response);

        mockMvc.perform(put("/branches/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(TestFixtures.validBranchRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Central Branch"));
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