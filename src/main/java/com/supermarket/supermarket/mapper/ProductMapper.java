package com.supermarket.supermarket.mapper;

import org.springframework.stereotype.Component;

import com.supermarket.supermarket.dto.product.ProductRequest;
import com.supermarket.supermarket.dto.product.ProductResponse;
import com.supermarket.supermarket.model.Product;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductMapper {

    public ProductResponse toResponse(Product product) {
        if (product == null)
            return null;

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .category(product.getCategory())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .build();
    }

    public Product toEntity(ProductRequest request) {
        if (request == null)
            return null;

        return Product.builder()
                .name(request.getName())
                .category(request.getCategory())
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .build();
    }

    public void updateEntity(ProductRequest request, Product target) {
        if (request == null || target == null)
            return;

        if (request.getName() != null) {
            target.setName(request.getName());
        }

        if (request.getCategory() != null) {
            target.setCategory(request.getCategory());
        }

        if (request.getPrice() != null) {
            target.setPrice(request.getPrice());
        }

        if (request.getQuantity() != null) {
            target.setQuantity(request.getQuantity());
        }
    }

    public List<ProductResponse> toResponseList(List<Product> entities) {
        if (entities == null)
            return null;

        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}