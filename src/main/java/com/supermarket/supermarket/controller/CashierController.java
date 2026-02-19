package com.supermarket.supermarket.controller;

import com.supermarket.supermarket.dto.sale.SaleResponse;
import com.supermarket.supermarket.model.User;
import com.supermarket.supermarket.service.business.SaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cashier")
@RequiredArgsConstructor
@Tag(name = "Cashier", description = "Endpoints for cashier personal sales management")
@SecurityRequirement(name = "Bearer Authentication")
public class CashierController {
    private final SaleService saleService;

    @GetMapping("/my-sales")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    @Operation(summary = "Get my sales paginated")
    public ResponseEntity<Page<SaleResponse>> getMySales(
            @PageableDefault(page = 0, size = 10, sort = "createdAt") Pageable pageable) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(saleService.getSalesByCashier(currentUser.getId(), pageable));
    }

    @GetMapping("/my-sales/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    @Operation(summary = "Get my sale by ID")
    public ResponseEntity<SaleResponse> getMySaleById(@PathVariable Long id) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(saleService.getSaleByIdAndCashier(id, currentUser.getId()));
    }
}