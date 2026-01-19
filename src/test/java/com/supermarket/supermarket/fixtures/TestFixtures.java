package com.supermarket.supermarket.fixtures;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.supermarket.supermarket.dto.branch.BranchRequest;
import com.supermarket.supermarket.dto.branch.BranchResponse;
import com.supermarket.supermarket.dto.product.ProductRequest;
import com.supermarket.supermarket.dto.product.ProductResponse;
import com.supermarket.supermarket.dto.sale.SaleRequest;
import com.supermarket.supermarket.dto.sale.SaleResponse;
import com.supermarket.supermarket.dto.saleDetail.SaleDetailRequest;
import com.supermarket.supermarket.dto.saleDetail.SaleDetailResponse;
import com.supermarket.supermarket.model.SaleDetail;
import com.supermarket.supermarket.model.SaleStatus;
import com.supermarket.supermarket.model.Product;
import com.supermarket.supermarket.model.Branch;
import com.supermarket.supermarket.model.Sale;

public class TestFixtures {

        public static Product defaultProduct() {
                return productWithIdAndStock(1L, 100);
        }

        public static Product productWithIdAndStock(Long id, int stock) {
                return Product.builder()
                                .id(id)
                                .name("Premium Rice")
                                .category("Food")
                                .price(2.50)
                                .quantity(stock)
                                .build();
        }

        public static ProductRequest validProductRequest() {
                return ProductRequest.builder()
                                .name("New Product")
                                .category("Cleaning")
                                .price(10.0)
                                .quantity(50)
                                .build();
        }

        public static ProductRequest invalidProductRequest() {
                return ProductRequest.builder()
                                .name("")
                                .category("")
                                .price(-10.0)
                                .quantity(-5)
                                .build();
        }

        public static ProductResponse productResponse() {
                return ProductResponse.builder()
                                .id(1L)
                                .name("Premium Rice")
                                .category("Food")
                                .price(2.50)
                                .quantity(100)
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
                                                                .quantity(5)
                                                                .build()))
                                .build();
        }

        public static SaleRequest saleRequestWithMultipleProducts() {
                return SaleRequest.builder()
                                .branchId(1L)
                                .date(LocalDate.now())
                                .details(List.of(
                                                SaleDetailRequest.builder().productId(1L).quantity(2).build(),
                                                SaleDetailRequest.builder().productId(2L).quantity(3).build()))
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
                                .total(12.50)
                                .status(SaleStatus.REGISTERED)
                                .branch(defaultBranch())
                                .details(new ArrayList<>())
                                .build();
                SaleDetail detail = SaleDetail.builder()
                                .quantity(5)
                                .price(2.50)
                                .product(defaultProduct())
                                .sale(sale)
                                .build();

                sale.getDetails().add(detail);
                return sale;
        }

        public static SaleResponse saleResponse() {
                return SaleResponse.builder()
                                .id(100L)
                                .total(12.50)
                                .date(LocalDate.now())
                                .status(SaleStatus.REGISTERED)
                                .branchId(1L)
                                .branchName("Central Branch")
                                .details(List.of(
                                                SaleDetailResponse.builder()
                                                                .productName("Premium Rice")
                                                                .quantity(5)
                                                                .unitPrice(2.50)
                                                                .subtotal(12.50)
                                                                .build()))
                                .build();
        }
}