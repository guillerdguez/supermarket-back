package com.supermarket.supermarket.controller;

import com.supermarket.supermarket.dto.sale.CancelSaleRequest;
import com.supermarket.supermarket.dto.sale.SaleRequest;
import com.supermarket.supermarket.dto.sale.SaleResponse;
import com.supermarket.supermarket.service.business.SaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/sales")
@RequiredArgsConstructor
@Tag(name = "Sales", description = "Endpoints for supermarket sales management")
@SecurityRequirement(name = "Bearer Authentication")
public class SaleController {
    private final SaleService saleService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Retrieve all sales - Requires ADMIN or MANAGER role")
    public ResponseEntity<List<SaleResponse>> getAll() {
        return ResponseEntity.ok(saleService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    @Operation(summary = "Retrieve a sale by ID")
    public ResponseEntity<SaleResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(saleService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    @Operation(summary = "Create a new sale")
    public ResponseEntity<SaleResponse> create(@Valid @RequestBody SaleRequest request) {
        SaleResponse created = saleService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Cancel a sale - Requires ADMIN or MANAGER role")
    public ResponseEntity<SaleResponse> cancel(
            @PathVariable Long id,
            @Valid @RequestBody CancelSaleRequest request) {
        return ResponseEntity.ok(saleService.cancel(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a sale - Requires ADMIN role only")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        saleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}