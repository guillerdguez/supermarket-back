package com.supermercado.supermercado.controller;

import com.supermercado.supermercado.dto.ventaDto.VentaRequest;
import com.supermercado.supermercado.dto.ventaDto.VentaResponse;
import com.supermercado.supermercado.service.VentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/sales")
@RequiredArgsConstructor
@Tag(name = "Sales", description = "Endpoints for supermarket sales management")
public class VentaController {

    private final VentaService ventaService;

    @GetMapping
    @Operation(summary = "Retrieve all sales")
    public ResponseEntity<List<VentaResponse>> getAll() {
        return ResponseEntity.ok(ventaService.getAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Retrieve a sale by ID")
    public ResponseEntity<VentaResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ventaService.getById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new sale")
    public ResponseEntity<VentaResponse> create(@Valid @RequestBody VentaRequest request) {
        VentaResponse created = ventaService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing sale")
    public ResponseEntity<VentaResponse> update(@PathVariable Long id, @Valid @RequestBody VentaRequest request) {
        return ResponseEntity.ok(ventaService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a sale")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        ventaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}