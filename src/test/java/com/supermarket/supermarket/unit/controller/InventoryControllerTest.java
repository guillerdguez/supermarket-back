package com.supermarket.supermarket.unit.controller;

import com.supermarket.supermarket.controller.InventoryController;
import com.supermarket.supermarket.dto.inventory.BranchInventoryResponse;
import com.supermarket.supermarket.exception.GlobalExceptionHandler;
import com.supermarket.supermarket.exception.ResourceNotFoundException;
import com.supermarket.supermarket.service.business.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class InventoryControllerTest {

    private MockMvc mockMvc;
    @Mock
    private InventoryService inventoryService;

    @BeforeEach
    void setUp() {
        InventoryController inventoryController = new InventoryController(inventoryService);

        mockMvc = MockMvcBuilders.standaloneSetup(inventoryController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private List<BranchInventoryResponse> sampleInventory() {
        return List.of(
                BranchInventoryResponse.builder()
                        .id(1L)
                        .branchId(5L)
                        .branchName("Central Branch")
                        .productId(10L)
                        .productName("Premium Rice")
                        .productCategory("Food")
                        .stock(50)
                        .minStock(10)
                        .lastRestockDate(LocalDateTime.now().minusDays(2))
                        .build()
        );
    }

    @Test
    @DisplayName("GET /inventory/branches/{branchId}/inventory - should return branch inventory")
    void getBranchInventory_ShouldReturnList() throws Exception {
        Long branchId = 5L;
        given(inventoryService.getBranchInventory(branchId))
                .willReturn(sampleInventory());

        mockMvc.perform(get("/inventory/branches/{branchId}/inventory", branchId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].branchId").value(5))
                .andExpect(jsonPath("$[0].productName").value("Premium Rice"))
                .andExpect(jsonPath("$[0].stock").value(50));

        then(inventoryService).should().getBranchInventory(branchId);
    }

    @Test
    @DisplayName("GET /inventory/branches/{branchId}/inventory - should return 404 when branch not found")
    void getBranchInventory_WhenBranchNotFound_ShouldReturn404() throws Exception {
        Long branchId = 999L;
        given(inventoryService.getBranchInventory(branchId))
                .willThrow(new ResourceNotFoundException("Branch not found with id: 999"));

        mockMvc.perform(get("/inventory/branches/{branchId}/inventory", branchId))
                .andExpect(status().isNotFound());

        then(inventoryService).should().getBranchInventory(branchId);
    }

    @Test
    @DisplayName("GET /inventory/branches/{branchId}/inventory - should return empty list when no inventory")
    void getBranchInventory_WhenNoInventory_ShouldReturnEmptyList() throws Exception {
        Long branchId = 10L;
        given(inventoryService.getBranchInventory(branchId))
                .willReturn(List.of());

        mockMvc.perform(get("/inventory/branches/{branchId}/inventory", branchId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        then(inventoryService).should().getBranchInventory(branchId);
    }

    @Test
    @DisplayName("GET /inventory/branches/{branchId}/inventory - should return 400 when branchId is not a valid number")
    void getBranchInventory_WithNonNumericBranchId_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/inventory/branches/{branchId}/inventory", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid value 'abc' for parameter 'branchId'"));

        then(inventoryService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("GET /inventory/branches/{branchId}/inventory - should return 500 when service throws an unexpected exception")
    void getBranchInventory_WhenUnexpectedException_ShouldReturn500() throws Exception {
        Long branchId = 5L;
        given(inventoryService.getBranchInventory(branchId))
                .willThrow(new RuntimeException("Unexpected database failure"));

        mockMvc.perform(get("/inventory/branches/{branchId}/inventory", branchId))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred. Please contact support."));

        then(inventoryService).should().getBranchInventory(branchId);
    }
}