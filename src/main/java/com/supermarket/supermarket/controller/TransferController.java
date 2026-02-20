package com.supermarket.supermarket.controller;

import com.supermarket.supermarket.dto.transfer.RejectTransferRequest;
import com.supermarket.supermarket.dto.transfer.TransferRequest;
import com.supermarket.supermarket.dto.transfer.TransferResponse;
import com.supermarket.supermarket.service.business.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/transfers")
@RequiredArgsConstructor
@Tag(name = "Transfers", description = "Endpoints for stock transfers between branches")
@SecurityRequirement(name = "Bearer Authentication")
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    @Operation(summary = "Request a stock transfer between branches")
    public ResponseEntity<TransferResponse> requestTransfer(@Valid @RequestBody TransferRequest request) {
        TransferResponse response = transferService.requestTransfer(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get all transfers - Requires ADMIN or MANAGER role")
    public ResponseEntity<List<TransferResponse>> getAllTransfers() {
        return ResponseEntity.ok(transferService.getAllTransfers());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    @Operation(summary = "Get a transfer by ID")
    public ResponseEntity<TransferResponse> getTransferById(@PathVariable Long id) {
        return ResponseEntity.ok(transferService.getTransferById(id));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Approve a pending transfer - Requires ADMIN or MANAGER role")
    public ResponseEntity<TransferResponse> approveTransfer(@PathVariable Long id) {
        return ResponseEntity.ok(transferService.approveTransfer(id));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Reject a pending transfer with reason - Requires ADMIN or MANAGER role")
    public ResponseEntity<TransferResponse> rejectTransfer(
            @PathVariable Long id,
            @Valid @RequestBody RejectTransferRequest request) {
        return ResponseEntity.ok(transferService.rejectTransfer(id, request));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Complete an approved transfer and move stock - Requires ADMIN or MANAGER role")
    public ResponseEntity<TransferResponse> completeTransfer(@PathVariable Long id) {
        return ResponseEntity.ok(transferService.completeTransfer(id));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    @Operation(summary = "Cancel a pending transfer - Requester or ADMIN only")
    public ResponseEntity<TransferResponse> cancelTransfer(@PathVariable Long id) {
        return ResponseEntity.ok(transferService.cancelTransfer(id));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get transfers filtered by status")
    public ResponseEntity<List<TransferResponse>> getTransfersByStatus(@PathVariable String status) {
        return ResponseEntity.ok(transferService.getTransfersByStatus(status));
    }

    @GetMapping("/source/{branchId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get transfers by source branch")
    public ResponseEntity<List<TransferResponse>> getTransfersBySourceBranch(@PathVariable Long branchId) {
        return ResponseEntity.ok(transferService.getTransfersBySourceBranch(branchId));
    }

    @GetMapping("/target/{branchId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get transfers by target branch")
    public ResponseEntity<List<TransferResponse>> getTransfersByTargetBranch(@PathVariable Long branchId) {
        return ResponseEntity.ok(transferService.getTransfersByTargetBranch(branchId));
    }
}