package com.supermarket.supermarket.dto.inventory;

import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

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

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CashRegisterFilterRequest {
        @PastOrPresent(message = "Start date cannot be in the future")
        private LocalDate startDate;
        @PastOrPresent(message = "End date cannot be in the future")
        private LocalDate endDate;
        private Long branchId;
        private boolean showOnlyDiscrepancies;
    }
}