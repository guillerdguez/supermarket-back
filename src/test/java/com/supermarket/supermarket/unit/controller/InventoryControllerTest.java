package com.supermarket.supermarket.unit.controller;

import com.supermarket.supermarket.controller.InventoryController;
import com.supermarket.supermarket.dto.inventory.BranchInventoryResponse;
import com.supermarket.supermarket.dto.inventory.StockAdjustmentRequest;
import com.supermarket.supermarket.dto.inventory.StockUpdateRequest;
import com.supermarket.supermarket.dto.inventory.TotalStockResponse;
import com.supermarket.supermarket.exception.GlobalExceptionHandler;
import com.supermarket.supermarket.exception.ResourceNotFoundException;
import com.supermarket.supermarket.service.business.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

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

    @PutMapping("/branches/{branchId}/products/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Set stock and minimum stock for a product in a branch")
    public ResponseEntity<BranchInventoryResponse> updateStock(
            @PathVariable Long branchId,
            @PathVariable Long productId,
            @Valid @RequestBody StockUpdateRequest request) {
        return ResponseEntity.ok(inventoryService.updateStock(branchId, productId, request));
    }

    @PatchMapping("/branches/{branchId}/products/{productId}/adjust")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Adjust stock by a positive or negative delta")
    public ResponseEntity<BranchInventoryResponse> adjustStock(
            @PathVariable Long branchId,
            @PathVariable Long productId,
            @Valid @RequestBody StockAdjustmentRequest request) {
        return ResponseEntity.ok(inventoryService.adjustStock(branchId, productId, request));
    }

    @GetMapping("/products/{productId}/total-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get total stock of a product across all branches")
    public ResponseEntity<TotalStockResponse> getTotalStock(@PathVariable Long productId) {
        return ResponseEntity.ok(inventoryService.getTotalStockByProduct(productId));
    }
}