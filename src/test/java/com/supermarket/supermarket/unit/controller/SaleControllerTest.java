package com.supermarket.supermarket.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.supermarket.supermarket.controller.SaleController;
import com.supermarket.supermarket.dto.sale.SaleResponse;
import com.supermarket.supermarket.exception.GlobalExceptionHandler;
import com.supermarket.supermarket.exception.ResourceNotFoundException;
import com.supermarket.supermarket.service.business.SaleService;
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

import static com.supermarket.supermarket.fixtures.sale.SaleFixtures.*;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class SaleControllerTest {
    private MockMvc mockMvc;
    @Mock
    private SaleService saleService;
    private ObjectMapper objectMapper;
    private SaleController saleController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        saleController = new SaleController(saleService);
        mockMvc = MockMvcBuilders.standaloneSetup(saleController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /sales - should return list")
    void getAll_ShouldReturnList() throws Exception {
        given(saleService.getAll()).willReturn(List.of(saleResponse()));
        mockMvc.perform(get("/sales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("GET /sales/{id} - should return sale")
    void getById_ShouldReturnSale() throws Exception {
        given(saleService.getById(100L)).willReturn(saleResponse());
        mockMvc.perform(get("/sales/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(12.50));
    }

    @Test
    @DisplayName("GET /sales/{id} - should return 404 when not found")
    void getById_WhenNotFound_ShouldReturn404() throws Exception {
        given(saleService.getById(999L))
                .willThrow(new ResourceNotFoundException("Sale not found"));
        mockMvc.perform(get("/sales/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /sales - should create sale")
    void create_ShouldReturn201() throws Exception {
        SaleResponse response = saleResponse();
        given(saleService.create(any())).willReturn(response);
        mockMvc.perform(post("/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validSaleRequest())))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.total").value(12.50));
    }

    @Test
    @DisplayName("POST /sales - should return 400 when invalid request")
    void create_WithInvalidRequest_ShouldReturn400() throws Exception {
        mockMvc.perform(post("/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidSaleRequest())))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /sales/{id} - should update sale")
    void update_ShouldReturnUpdatedSale() throws Exception {
        SaleResponse response = saleResponse();
        given(saleService.update(eq(100L), any())).willReturn(response);
        mockMvc.perform(put("/sales/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validSaleRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(12.50));
    }

    @Test
    @DisplayName("DELETE /sales/{id} - should return 204")
    void delete_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/sales/100"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /sales/{id} - should return 404 when not found")
    void delete_WhenNotFound_ShouldReturn404() throws Exception {
        doThrow(new ResourceNotFoundException("Sale not found"))
                .when(saleService).delete(999L);
        mockMvc.perform(delete("/sales/999"))
                .andExpect(status().isNotFound());
    }
}