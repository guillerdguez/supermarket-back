package com.supermarket.supermarket.fixtures;

import com.supermarket.supermarket.dto.auth.RegisterRequest;
import com.supermarket.supermarket.dto.branch.BranchRequest;
import com.supermarket.supermarket.dto.branch.BranchResponse;
import com.supermarket.supermarket.dto.product.ProductRequest;
import com.supermarket.supermarket.dto.product.ProductResponse;
import com.supermarket.supermarket.dto.sale.SaleRequest;
import com.supermarket.supermarket.dto.sale.SaleResponse;
import com.supermarket.supermarket.dto.saleDetail.SaleDetailRequest;
import com.supermarket.supermarket.dto.saleDetail.SaleDetailResponse;
import com.supermarket.supermarket.model.Branch;
import com.supermarket.supermarket.model.Product;
import com.supermarket.supermarket.model.Sale;
import com.supermarket.supermarket.model.SaleDetail;
import com.supermarket.supermarket.model.SaleStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TestFixtures {

    public static RegisterRequest userRegisterRequest() {
        return RegisterRequest.builder()
                .username("test-user")
                .email("user@test.com")
                .password("Password123!")
                .firstName("Test")
                .lastName("User")
                .build();
    }

    public static RegisterRequest adminRegisterRequest() {
        return RegisterRequest.builder()
                .username("admin-test")
                .email("admin@test.com")
                .password("Admin123!")
                .firstName("Admin")
                .lastName("Test")
                .build();
    }

    public static RegisterRequest cashierRegisterRequest() {
        return RegisterRequest.builder()
                .username("cashier-test")
                .email("cashier@test.com")
                .password("Cashier123!")
                .firstName("Cashier")
                .lastName("Test")
                .build();
    }

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

    public static Branch defaultBranch() {
        return Branch.builder()
                .id(1L)
                .name("Central Branch")
                .address("123 Main St")
                .build();
    }

    public static BranchRequest validBranchRequest() {
        return BranchRequest.builder()
                .name("New Branch")
                .address("456 North Ave")
                .build();
    }

    public static BranchRequest invalidBranchRequest() {
        return BranchRequest.builder()
                .name("")
                .address("")
                .build();
    }

    public static BranchResponse branchResponse() {
        return BranchResponse.builder()
                .id(1L)
                .name("Central Branch")
                .address("123 Main St")
                .build();
    }

    public static SaleRequest validSaleRequest() {
        return SaleRequest.builder()
                .branchId(1L)
                .date(LocalDate.now())
                .details(List.of(
                        SaleDetailRequest.builder()
                                .productId(1L)
                                .stock(5)
                                .build()))
                .build();
    }

    public static SaleRequest invalidSaleRequest() {
        return SaleRequest.builder()
                .branchId(null)
                .details(List.of())
                .build();
    }

    public static Sale saleWithDetails() {
        Sale sale = Sale.builder()
                .id(100L)
                .date(LocalDate.now())
                .total(new BigDecimal("12.50"))
                .status(SaleStatus.REGISTERED)
                .branch(defaultBranch())
                .details(new ArrayList<>())
                .build();

        SaleDetail detail = SaleDetail.builder()
                .stock(5)
                .price(new BigDecimal("2.50"))
                .product(defaultProduct())
                .sale(sale)
                .build();
        sale.getDetails().add(detail);
        return sale;
    }

    public static SaleResponse saleResponse() {
        return SaleResponse.builder()
                .id(100L)
                .total(new BigDecimal("12.50"))
                .date(LocalDate.now())
                .status(SaleStatus.REGISTERED)
                .branchId(1L)
                .branchName("Central Branch")
                .details(List.of(
                        SaleDetailResponse.builder()
                                .productName("Premium Rice")
                                .stock(5)
                                .unitPrice(new BigDecimal("2.50"))
                                .subtotal(new BigDecimal("12.50"))
                                .build()))
                .build();
    }
}