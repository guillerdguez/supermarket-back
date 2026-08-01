package com.supermarket.supermarket.controller;

import com.supermarket.supermarket.dto.inventory.BranchInventoryResponse;
import com.supermarket.supermarket.dto.inventory.LowStockAlertResponse;
import com.supermarket.supermarket.dto.inventory.StockAdjustmentRequest;
import com.supermarket.supermarket.dto.inventory.StockUpdateRequest;
import com.supermarket.supermarket.dto.inventory.TotalStockResponse;
import com.supermarket.supermarket.service.business.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory")
@SecurityRequirement(name = "Bearer Authentication")
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/low-stock")
    @Operation(summary = "Get global low stock alerts")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<LowStockAlertResponse>> getLowStockGlobal() {
        return ResponseEntity.ok(inventoryService.getLowStockGlobal());
    }

    @GetMapping("/branches/{branchId}/low-stock")
    @Operation(summary = "Get low stock alerts for a specific branch")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<LowStockAlertResponse>> getLowStockByBranch(@PathVariable Long branchId) {
        return ResponseEntity.ok(inventoryService.getLowStockInBranch(branchId));
    }

    @GetMapping("/branches/{branchId}/inventory")
    @Operation(summary = "Get complete inventory for a specific branch")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<List<BranchInventoryResponse>> getBranchInventory(@PathVariable Long branchId) {
        return ResponseEntity.ok(inventoryService.getBranchInventory(branchId));
    }

    @GetMapping("/branches/{branchId}/products/{productId}")
    @Operation(summary = "Get stock of a product in a branch")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<Integer> getStockInBranch(
            @PathVariable Long branchId,
            @PathVariable Long productId) {
        return ResponseEntity.ok(inventoryService.getStockInBranch(branchId, productId));
    }

    @GetMapping("/products/{productId}/total-stock")
    @Operation(summary = "Get total stock of a product across all branches")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<TotalStockResponse> getTotalStock(@PathVariable Long productId) {
        return ResponseEntity.ok(inventoryService.getTotalStockByProduct(productId));
    }

    @PutMapping("/branches/{branchId}/products/{productId}")
    @Operation(summary = "Set stock and minimum stock for a product in a branch")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<BranchInventoryResponse> updateStock(
            @PathVariable Long branchId,
            @PathVariable Long productId,
            @Valid @RequestBody StockUpdateRequest request) {
        return ResponseEntity.ok(inventoryService.updateStock(branchId, productId, request));
    }

    @PatchMapping("/branches/{branchId}/products/{productId}/adjust")
    @Operation(summary = "Adjust stock by a positive or negative delta")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<BranchInventoryResponse> adjustStock(
            @PathVariable Long branchId,
            @PathVariable Long productId,
            @Valid @RequestBody StockAdjustmentRequest request) {
        return ResponseEntity.ok(inventoryService.adjustStock(branchId, productId, request));
    }
}