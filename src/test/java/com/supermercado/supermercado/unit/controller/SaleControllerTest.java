package com.supermercado.supermercado.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supermercado.supermercado.controller.VentaController;
import com.supermercado.supermercado.dto.ventaDto.VentaRequest;
import com.supermercado.supermercado.dto.ventaDto.VentaResponse;
import com.supermercado.supermercado.exception.ResourceNotFoundException;
import com.supermercado.supermercado.fixtures.TestFixtures;
import com.supermercado.supermercado.service.VentaService;
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

@WebMvcTest(VentaController.class)
class SaleControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private VentaService saleService;

    @Test
    @DisplayName("GET /sales - should return list")
    void getAll_ShouldReturnList() throws Exception {
        given(saleService.getAll()).willReturn(List.of(TestFixtures.saleResponse()));

        mockMvc.perform(get("/sales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("GET /sales/{id} - should return sale")
    void getById_ShouldReturnSale() throws Exception {
        given(saleService.getById(100L)).willReturn(TestFixtures.saleResponse());

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
        VentaResponse response = TestFixtures.saleResponse();
        given(saleService.create(any(VentaRequest.class))).willReturn(response);

        mockMvc.perform(post("/sales")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(TestFixtures.validSaleRequest())))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.total").value(12.50));
    }

    @Test
    @DisplayName("POST /sales - should return 400 when invalid request")
    void create_WithInvalidRequest_ShouldReturn400() throws Exception {
        mockMvc.perform(post("/sales")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(TestFixtures.invalidSaleRequest())))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /sales/{id} - should update sale")
    void update_ShouldReturnUpdatedSale() throws Exception {
        VentaResponse response = TestFixtures.saleResponse();
        given(saleService.update(eq(100L), any(VentaRequest.class))).willReturn(response);

        mockMvc.perform(put("/sales/100")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(TestFixtures.validSaleRequest())))
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