package com.supermarket.supermarket.fixtures.product;

import com.supermarket.supermarket.dto.product.ProductRequest;
import com.supermarket.supermarket.dto.product.ProductResponse;
import com.supermarket.supermarket.model.product.Product;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;

@UtilityClass
public class ProductFixtures {
    public static Product defaultProduct() {
        return productWithId(1L);
    }

    public static Product productWithId(Long id) {
        return Product.builder()
                .id(id)
                .name("Premium Rice")
                .barcode("8410000000001")
                .category("Food")
                .price(new BigDecimal("2.50"))
                .build();
    }

    public static ProductRequest validProductRequest() {
        return ProductRequest.builder()
                .name("New Product")
                .barcode("8410000000099")
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
                .barcode("8410000000001")
                .category("Food")
                .price(new BigDecimal("2.50"))
                .build();
    }
}