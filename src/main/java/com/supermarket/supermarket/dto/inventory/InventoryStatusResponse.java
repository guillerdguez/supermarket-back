package com.supermarket.supermarket.dto.inventory;

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
public class InventoryStatusResponse {
    private Long totalProducts;
    private Long totalUnitsInStock;
    private BigDecimal totalInventoryValue;
    private Long lowStockCount;
    private Long outOfStockCount;
}