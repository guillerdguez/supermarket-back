package com.supermarket.supermarket.service;

import org.springframework.data.jpa.domain.Specification;

import com.supermarket.supermarket.dto.product.ProductRequest;
import com.supermarket.supermarket.dto.product.ProductResponse;
import com.supermarket.supermarket.dto.saleDetail.SaleDetailRequest;
import com.supermarket.supermarket.model.Product;
import com.supermarket.supermarket.model.SaleDetail;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    Page<ProductResponse> getAll(Specification<Product> spec, Pageable pageable);

    List<ProductResponse> getAllForDropdown();

    ProductResponse getById(Long id);

    ProductResponse create(ProductRequest product);

    ProductResponse update(Long id, ProductRequest product);

    void delete(Long id);

    List<Product> validateAndReduceStockBatch(List<SaleDetailRequest> details);

    void restoreStockBatch(List<SaleDetail> existingDetails);

}