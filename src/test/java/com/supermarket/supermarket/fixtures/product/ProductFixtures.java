package com.supermarket.supermarket.fixtures.product;

import com.supermarket.supermarket.dto.product.ProductRequest;
import com.supermarket.supermarket.dto.product.ProductResponse;
import com.supermarket.supermarket.model.product.Product;

import java.math.BigDecimal;

public class ProductFixtures {
    public static Product defaultProduct() {
        return productWithId(1L);
    }

    public static Product productWithId(Long id) {
        return Product.builder()
                .id(id)
                .name("Premium Rice")
                .category("Food")
                .price(new BigDecimal("2.50"))
                .build();
    }

    public static ProductRequest validProductRequest() {
        return ProductRequest.builder()
                .name("New Product")
                .category("Cleaning")
                .price(new BigDecimal("10.00"))
                .build();
    }

    public static ProductRequest invalidProductRequest() {
        return ProductRequest.builder()
                .name("")
                .category("")
                .price(new BigDecimal("-10.00"))
                .build();
    }

    public static ProductResponse productResponse() {
        return ProductResponse.builder()
                .id(1L)
                .name("Premium Rice")
                .category("Food")
                .price(new BigDecimal("2.50"))
                .build();
    }
}