package com.supermarket.supermarket.controller;

import com.supermarket.supermarket.dto.sale.SaleResponse;
import com.supermarket.supermarket.model.user.User;
import com.supermarket.supermarket.security.SecurityUtils;
import com.supermarket.supermarket.service.business.SaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/cashier")
@RequiredArgsConstructor
@Tag(name = "Cashier", description = "Endpoints for cashier personal sales management")
@SecurityRequirement(name = "Bearer Authentication")
public class CashierController {
    private final SaleService saleService;
    private final SecurityUtils securityUtils;

    @GetMapping("/my-sales")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    @Operation(summary = "Get my sales")
    public ResponseEntity<List<SaleResponse>> getMySales() {
        User currentUser = securityUtils.getCurrentUser();
        return ResponseEntity.ok(saleService.getSalesByCashier(currentUser.getId()));
    }

    @GetMapping("/my-sales/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    @Operation(summary = "Get my sale by ID")
    public ResponseEntity<SaleResponse> getMySaleById(@PathVariable Long id) {
        User currentUser = securityUtils.getCurrentUser();
        return ResponseEntity.ok(saleService.getSaleByIdAndCashier(id, currentUser.getId()));
    }
}