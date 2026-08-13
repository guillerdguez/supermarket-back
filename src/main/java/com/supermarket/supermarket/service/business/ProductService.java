package com.supermarket.supermarket.service.business;

import com.supermarket.supermarket.dto.product.ProductRequest;
import com.supermarket.supermarket.dto.product.ProductResponse;
import com.supermarket.supermarket.model.product.Product;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface ProductService {

    List<ProductResponse> getAll(Specification<Product> spec);

    ProductResponse getById(Long id);

    ProductResponse create(ProductRequest product);

    ProductResponse update(Long id, ProductRequest product);

    void delete(Long id);

}
