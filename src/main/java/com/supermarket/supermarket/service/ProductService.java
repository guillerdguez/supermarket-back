package com.supermarket.supermarket.service;

import com.supermarket.supermarket.dto.product.ProductRequest;
import com.supermarket.supermarket.dto.product.ProductResponse;
import com.supermarket.supermarket.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface ProductService {

    Page<ProductResponse> getAll(Specification<Product> spec, Pageable pageable);

    List<ProductResponse> getAllForDropdown();

    ProductResponse getById(Long id);

    ProductResponse create(ProductRequest product);

    ProductResponse update(Long id, ProductRequest product);

    void delete(Long id);
}
