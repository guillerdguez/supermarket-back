package com.supermarket.supermarket.service;

import java.util.List;

import com.supermarket.supermarket.dto.product.ProductRequest;
import com.supermarket.supermarket.dto.product.ProductResponse;
import com.supermarket.supermarket.model.Product;

public interface ProductService {
    List<ProductResponse> getAll();

    ProductResponse getById(Long id);

    ProductResponse create(ProductRequest product);

    ProductResponse update(Long id, ProductRequest product);

    void delete(Long id);

    Product reduceStock(Long id, Integer quantity);

    void increaseStock(Long id, Integer quantity);
}