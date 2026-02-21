package com.supermarket.supermarket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesByProductDTO {
    private Long productId;
    private String productName;
    private String productCategory;
    private Long totalQuantitySold;
    private BigDecimal totalRevenue;
}