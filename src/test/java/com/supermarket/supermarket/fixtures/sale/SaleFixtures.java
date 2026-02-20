package com.supermarket.supermarket.fixtures.sale;

import com.supermarket.supermarket.dto.sale.CancelSaleRequest;
import com.supermarket.supermarket.dto.sale.SaleRequest;
import com.supermarket.supermarket.dto.sale.SaleResponse;
import com.supermarket.supermarket.dto.saleDetail.SaleDetailRequest;
import com.supermarket.supermarket.dto.saleDetail.SaleDetailResponse;
import com.supermarket.supermarket.fixtures.branch.BranchFixtures;
import com.supermarket.supermarket.fixtures.product.ProductFixtures;
import com.supermarket.supermarket.fixtures.user.UserFixtures;
import com.supermarket.supermarket.model.*;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class SaleFixtures {

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

    public static CancelSaleRequest validCancelRequest() {
        return CancelSaleRequest.builder()
                .reason("Customer returned items")
                .build();
    }

    public static CancelSaleRequest invalidCancelRequest() {
        return CancelSaleRequest.builder()
                .reason("")
                .build();
    }

    public static Sale saleWithDetails() {
        Branch branch = BranchFixtures.defaultBranch();
        Product product = ProductFixtures.defaultProduct();
        User cashier = UserFixtures.defaultCashier();
        Sale sale = Sale.builder()
                .id(100L)
                .date(LocalDate.now())
                .total(new BigDecimal("12.50"))
                .status(SaleStatus.REGISTERED)
                .branch(branch)
                .createdBy(cashier)
                .createdAt(LocalDateTime.now())
                .details(new ArrayList<>())
                .build();
        SaleDetail detail = SaleDetail.builder()
                .stock(5)
                .price(new BigDecimal("2.50"))
                .product(product)
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
                .createdById(1L)
                .createdByUsername("cashier-test")
                .createdByEmail("cashier@test.com")
                .createdAt(LocalDateTime.now())
                .details(List.of(
                        SaleDetailResponse.builder()
                                .productName("Premium Rice")
                                .stock(5)
                                .unitPrice(new BigDecimal("2.50"))
                                .subtotal(new BigDecimal("12.50"))
                                .build()))
                .build();
    }

    public static SaleResponse cancelledSaleResponse() {
        return SaleResponse.builder()
                .id(100L)
                .total(new BigDecimal("12.50"))
                .date(LocalDate.now())
                .status(SaleStatus.CANCELLED)
                .branchId(1L)
                .branchName("Central Branch")
                .createdById(1L)
                .createdByUsername("cashier-test")
                .createdByEmail("cashier@test.com")
                .createdAt(LocalDateTime.now())
                .cancelledById(2L)
                .cancelledByUsername("manager-test")
                .cancellationReason("Customer returned items")
                .cancelledAt(LocalDateTime.now())
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