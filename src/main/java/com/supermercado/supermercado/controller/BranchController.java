package com.supermercado.supermercado.controller;

import com.supermercado.supermercado.dto.sucursalDto.SucursalRequest;
import com.supermercado.supermercado.dto.sucursalDto.SucursalResponse;
import com.supermercado.supermercado.service.SucursalService;
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
@RequestMapping("/branches")
@RequiredArgsConstructor
@Tag(name = "Branches", description = "Endpoints for branch management")
public class BranchController {

    private final SucursalService branchService;

    @GetMapping
    @Operation(summary = "Retrieve all branches")
    public ResponseEntity<List<SucursalResponse>> getAll() {
        return ResponseEntity.ok(branchService.getAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Retrieve a branch by ID")
    public ResponseEntity<SucursalResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(branchService.getById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new branch")
    public ResponseEntity<SucursalResponse> create(@Valid @RequestBody SucursalRequest request) {
        SucursalResponse created = branchService.create(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing branch")
    public ResponseEntity<SucursalResponse> update(@PathVariable Long id, @Valid @RequestBody SucursalRequest request) {
        return ResponseEntity.ok(branchService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a branch")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        branchService.delete(id);
        
        return ResponseEntity.noContent().build();
    }
}