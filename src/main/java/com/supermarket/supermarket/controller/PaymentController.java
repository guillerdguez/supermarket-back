package com.supermarket.supermarket.controller;

import com.supermarket.supermarket.dto.payment.PaymentRequest;
import com.supermarket.supermarket.dto.payment.PaymentResponse;
import com.supermarket.supermarket.service.business.PaymentService;
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
@RequestMapping("/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Endpoints for payment management")
@SecurityRequirement(name = "Bearer Authentication")
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    @Operation(summary = "Register a payment for a sale")
    public ResponseEntity<PaymentResponse> registerPayment(@Valid @RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.registerPayment(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/sale/{saleId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    @Operation(summary = "Get all payments for a sale")
    public ResponseEntity<List<PaymentResponse>> getPaymentsBySale(@PathVariable Long saleId) {
        return ResponseEntity.ok(paymentService.getPaymentsBySale(saleId));
    }
}