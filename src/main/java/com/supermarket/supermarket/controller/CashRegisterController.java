package com.supermarket.supermarket.controller;

import com.supermarket.supermarket.dto.cashregister.CashRegisterResponse;
import com.supermarket.supermarket.dto.cashregister.CloseRegisterRequest;
import com.supermarket.supermarket.dto.cashregister.OpenRegisterRequest;
import com.supermarket.supermarket.service.business.CashRegisterService;
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

@RestController
@RequestMapping("/cash-registers")
@RequiredArgsConstructor
@Tag(name = "Cash Register", description = "Endpoints for cash register management")
@SecurityRequirement(name = "Bearer Authentication")
public class CashRegisterController {
    private final CashRegisterService cashRegisterService;

    @PostMapping("/open")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    @Operation(summary = "Open a cash register for a branch")
    public ResponseEntity<CashRegisterResponse> openRegister(@Valid @RequestBody OpenRegisterRequest request) {
        CashRegisterResponse response = cashRegisterService.openRegister(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    @Operation(summary = "Close a cash register")
    public ResponseEntity<CashRegisterResponse> closeRegister(
            @PathVariable Long id,
            @Valid @RequestBody CloseRegisterRequest request) {
        return ResponseEntity.ok(cashRegisterService.closeRegister(id, request));
    }

    @GetMapping("/branches/{branchId}/current")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    @Operation(summary = "Get current open register for a branch")
    public ResponseEntity<CashRegisterResponse> getCurrentRegister(@PathVariable Long branchId) {
        return ResponseEntity.ok(cashRegisterService.getCurrentRegisterByBranch(branchId));
    }
}