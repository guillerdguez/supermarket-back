package com.supermarket.supermarket.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.supermarket.supermarket.controller.InventoryController;
import com.supermarket.supermarket.dto.inventory.BranchInventoryResponse;
import com.supermarket.supermarket.dto.inventory.StockAdjustmentRequest;
import com.supermarket.supermarket.dto.inventory.StockUpdateRequest;
import com.supermarket.supermarket.dto.inventory.TotalStockResponse;
import com.supermarket.supermarket.exception.GlobalExceptionHandler;
import com.supermarket.supermarket.exception.InsufficientStockException;
import com.supermarket.supermarket.exception.ResourceNotFoundException;
import com.supermarket.supermarket.service.business.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class InventoryControllerTest {

    private MockMvc mockMvc;
    @Mock
    private InventoryService inventoryService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        InventoryController inventoryController = new InventoryController(inventoryService);

        mockMvc = MockMvcBuilders.standaloneSetup(inventoryController).setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    private List<BranchInventoryResponse> sampleInventory() {
        return List.of(BranchInventoryResponse.builder().id(1L).branchId(5L).branchName("Central Branch").productId(10L).productName("Premium Rice").productCategory("Food").stock(50).minStock(10).lastRestockDate(LocalDateTime.now().minusDays(2)).build());
    }

    private BranchInventoryResponse sampleSingleInventory() {
        return BranchInventoryResponse.builder().id(100L).branchId(1L).branchName("Central Branch").productId(1L).productName("Premium Rice").productCategory("Food").stock(70).minStock(10).lastRestockDate(LocalDateTime.now()).build();
    }


    @Test
    @DisplayName("GET /inventory/branches/{branchId}/inventory - should return branch inventory")
    void getBranchInventory_ShouldReturnList() throws Exception {
        Long branchId = 5L;
        given(inventoryService.getBranchInventory(branchId)).willReturn(sampleInventory());

        mockMvc.perform(get("/inventory/branches/{branchId}/inventory", branchId)).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1))).andExpect(jsonPath("$[0].branchId").value(5)).andExpect(jsonPath("$[0].productName").value("Premium Rice")).andExpect(jsonPath("$[0].stock").value(50));

        then(inventoryService).should().getBranchInventory(branchId);
    }

    @Test
    @DisplayName("GET /inventory/branches/{branchId}/inventory - should return 404 when branch not found")
    void getBranchInventory_WhenBranchNotFound_ShouldReturn404() throws Exception {
        Long branchId = 999L;
        given(inventoryService.getBranchInventory(branchId)).willThrow(new ResourceNotFoundException("Branch not found with id: 999"));

        mockMvc.perform(get("/inventory/branches/{branchId}/inventory", branchId)).andExpect(status().isNotFound());

        then(inventoryService).should().getBranchInventory(branchId);
    }

    @Test
    @DisplayName("GET /inventory/branches/{branchId}/inventory - should return empty list when no inventory")
    void getBranchInventory_WhenNoInventory_ShouldReturnEmptyList() throws Exception {
        Long branchId = 10L;
        given(inventoryService.getBranchInventory(branchId)).willReturn(List.of());

        mockMvc.perform(get("/inventory/branches/{branchId}/inventory", branchId)).andExpect(status().isOk()).andExpect(jsonPath("$").isEmpty());

        then(inventoryService).should().getBranchInventory(branchId);
    }

    @Test
    @DisplayName("GET /inventory/branches/{branchId}/inventory - should return 400 when branchId is not a valid number")
    void getBranchInventory_WithNonNumericBranchId_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/inventory/branches/{branchId}/inventory", "abc")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").value("Bad Request")).andExpect(jsonPath("$.message").value("Invalid value 'abc' for parameter 'branchId'"));

        then(inventoryService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("GET /inventory/branches/{branchId}/inventory - should return 500 when service throws an unexpected exception")
    void getBranchInventory_WhenUnexpectedException_ShouldReturn500() throws Exception {
        Long branchId = 5L;
        given(inventoryService.getBranchInventory(branchId)).willThrow(new RuntimeException("Unexpected database failure"));

        mockMvc.perform(get("/inventory/branches/{branchId}/inventory", branchId)).andExpect(status().isInternalServerError()).andExpect(jsonPath("$.error").value("Internal Server Error")).andExpect(jsonPath("$.message").value("An unexpected error occurred. Please contact support."));

        then(inventoryService).should().getBranchInventory(branchId);
    }


    @Test
    @DisplayName("GET /inventory/products/{productId}/total-stock - should return total stock")
    void getTotalStock_ShouldReturnTotal() throws Exception {
        TotalStockResponse response = TotalStockResponse.builder().productId(1L).productName("Premium Rice").totalStock(150L).build();
        given(inventoryService.getTotalStockByProduct(1L)).willReturn(response);

        mockMvc.perform(get("/inventory/products/{productId}/total-stock", 1L)).andExpect(status().isOk()).andExpect(jsonPath("$.productId").value(1)).andExpect(jsonPath("$.productName").value("Premium Rice")).andExpect(jsonPath("$.totalStock").value(150));

        then(inventoryService).should().getTotalStockByProduct(1L);
    }

    @Test
    @DisplayName("GET /inventory/products/{productId}/total-stock - should return 404 when product not found")
    void getTotalStock_WhenProductNotFound_ShouldReturn404() throws Exception {
        given(inventoryService.getTotalStockByProduct(999L)).willThrow(new ResourceNotFoundException("Product not found with id: 999"));

        mockMvc.perform(get("/inventory/products/{productId}/total-stock", 999L)).andExpect(status().isNotFound());

        then(inventoryService).should().getTotalStockByProduct(999L);
    }


    @Test
    @DisplayName("PUT /inventory/branches/{branchId}/products/{productId} - should update stock and return 200")
    void updateStock_ShouldReturnUpdatedInventory() throws Exception {
        StockUpdateRequest request = StockUpdateRequest.builder().stock(70).minStock(10).build();
        given(inventoryService.updateStock(eq(1L), eq(1L), any(StockUpdateRequest.class))).willReturn(sampleSingleInventory());

        mockMvc.perform(put("/inventory/branches/{branchId}/products/{productId}", 1L, 1L).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isOk()).andExpect(jsonPath("$.stock").value(70)).andExpect(jsonPath("$.minStock").value(10)).andExpect(jsonPath("$.productName").value("Premium Rice"));

        then(inventoryService).should().updateStock(eq(1L), eq(1L), any(StockUpdateRequest.class));
    }

    @Test
    @DisplayName("PUT /inventory/branches/{branchId}/products/{productId} - should return 404 when inventory not found")
    void updateStock_WhenNotFound_ShouldReturn404() throws Exception {
        StockUpdateRequest request = StockUpdateRequest.builder().stock(10).minStock(5).build();
        given(inventoryService.updateStock(eq(1L), eq(99L), any(StockUpdateRequest.class))).willThrow(new ResourceNotFoundException("Product 99 not found in branch 1"));

        mockMvc.perform(put("/inventory/branches/{branchId}/products/{productId}", 1L, 99L).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /inventory/branches/{branchId}/products/{productId} - should return 400 when request is invalid")
    void updateStock_WithInvalidRequest_ShouldReturn400() throws Exception {
        // stock negativo → @Min(0) falla
        String invalidJson = """
                {"stock": -5, "minStock": 10}
                """;

        mockMvc.perform(put("/inventory/branches/{branchId}/products/{productId}", 1L, 1L).contentType(MediaType.APPLICATION_JSON).content(invalidJson)).andExpect(status().isBadRequest());

        then(inventoryService).shouldHaveNoInteractions();
    }


    @Test
    @DisplayName("PATCH /inventory/branches/{branchId}/products/{productId}/adjust - should adjust stock and return 200")
    void adjustStock_ShouldReturnUpdatedInventory() throws Exception {
        StockAdjustmentRequest request = StockAdjustmentRequest.builder().delta(15).reason("Manual restock").build();
        given(inventoryService.adjustStock(eq(1L), eq(1L), any(StockAdjustmentRequest.class))).willReturn(sampleSingleInventory());

        mockMvc.perform(patch("/inventory/branches/{branchId}/products/{productId}/adjust", 1L, 1L).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isOk()).andExpect(jsonPath("$.stock").value(70));

        then(inventoryService).should().adjustStock(eq(1L), eq(1L), any(StockAdjustmentRequest.class));
    }

    @Test
    @DisplayName("PATCH /inventory/branches/{branchId}/products/{productId}/adjust - should return 400 when adjustment would go negative")
    void adjustStock_WhenWouldGoNegative_ShouldReturn400() throws Exception {
        StockAdjustmentRequest request = StockAdjustmentRequest.builder().delta(-100).reason("Over-adjust").build();
        given(inventoryService.adjustStock(eq(1L), eq(1L), any(StockAdjustmentRequest.class))).willThrow(new InsufficientStockException("Adjustment would result in negative stock. Current: 50, delta: -100"));

        mockMvc.perform(patch("/inventory/branches/{branchId}/products/{productId}/adjust", 1L, 1L).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").value("Inventory Conflict"));
    }

    @Test
    @DisplayName("PATCH /inventory/branches/{branchId}/products/{productId}/adjust - should return 400 when delta is null")
    void adjustStock_WithNullDelta_ShouldReturn400() throws Exception {
        String invalidJson = """
                {"reason": "No delta"}
                """;

        mockMvc.perform(patch("/inventory/branches/{branchId}/products/{productId}/adjust", 1L, 1L).contentType(MediaType.APPLICATION_JSON).content(invalidJson)).andExpect(status().isBadRequest());

        then(inventoryService).shouldHaveNoInteractions();
    }
}