package com.supermarket.supermarket.dto.report;

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
public class ProductPerformanceDTO {
    private Long productId;
    private String productName;
    private String productCategory;
    private Long totalSold;
    private Integer currentStock;
    private BigDecimal inventoryTurnoverRate;
}