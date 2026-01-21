package com.supermarket.supermarket.controller;

import com.supermarket.supermarket.dto.product.ProductRequest;
import com.supermarket.supermarket.dto.product.ProductResponse;
import com.supermarket.supermarket.model.Product;
import com.supermarket.supermarket.service.ProductService;
import com.supermarket.supermarket.specification.ProductSpecifications;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/products")
@Tag(name = "Product", description = "Endpoints for supermarket product management")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @Operation(summary = "Get all products with pagination and filters")
    public ResponseEntity<Page<ProductResponse>> getAll(
            @PageableDefault(page = 0, size = 10, sort = "name") Pageable pageable,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {
        Specification<Product> spec = ProductSpecifications.withFilters(name, category, minPrice, maxPrice);
        return ResponseEntity.ok(productService.getAll(spec, pageable));
    }

    @GetMapping("/all")
    @Operation(summary = "Get all products as a simple list (for dropdowns)")
    public ResponseEntity<List<ProductResponse>> getAllList() {
        return ResponseEntity.ok(productService.getAllForDropdown());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a product by its ID")
    public ResponseEntity<ProductResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new product")
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        ProductResponse created = productService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing product")
    public ResponseEntity<ProductResponse> update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a product")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}