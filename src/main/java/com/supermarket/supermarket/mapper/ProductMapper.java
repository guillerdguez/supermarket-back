package com.supermarket.supermarket.mapper;

import com.supermarket.supermarket.dto.product.*;
import com.supermarket.supermarket.model.Product;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class ProductMapper {
    public ProductResponse toResponse(Product product) {
        if (product == null) return null;
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .category(product.getCategory())
                .price(product.getPrice())
                .build();
    }

    public Product toEntity(ProductRequest request) {
        if (request == null) return null;
        return Product.builder()
                .name(request.getName())
                .category(request.getCategory())
                .price(request.getPrice())
                .build();
    }

    public void updateEntity(ProductRequest request, Product target) {
        if (request == null || target == null) return;
        if (request.getName() != null) target.setName(request.getName());
        if (request.getCategory() != null) target.setCategory(request.getCategory());
        if (request.getPrice() != null) target.setPrice(request.getPrice());
    }

    public List<ProductResponse> toResponseList(List<Product> entities) {
        return entities.stream().map(this::toResponse).toList();
    }
}