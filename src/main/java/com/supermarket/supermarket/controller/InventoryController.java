package com.supermarket.supermarket.controller;

import com.supermarket.supermarket.dto.inventory.LowStockAlertResponse;
import com.supermarket.supermarket.service.business.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory")
@SecurityRequirement(name = "Bearer Authentication")
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<LowStockAlertResponse>> getLowStockGlobal() {
        return ResponseEntity.ok(inventoryService.getLowStockGlobal());
    }

    @GetMapping("/branches/{branchId}/low-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<LowStockAlertResponse>> getLowStockByBranch(@PathVariable Long branchId) {
        return ResponseEntity.ok(inventoryService.getLowStockInBranch(branchId));
    }

    @GetMapping("/branches/{branchId}/products/{productId}")
    public ResponseEntity<Integer> getStockInBranch(@PathVariable Long branchId, @PathVariable Long productId) {
        return ResponseEntity.ok(inventoryService.getStockInBranch(branchId, productId));
    }
}