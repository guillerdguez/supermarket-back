package com.supermarket.supermarket.dto;

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
public class SalesComparisonResponse {
    private PeriodSummary currentPeriod;
    private PeriodSummary previousPeriod;
    private BigDecimal growthPercentage;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PeriodSummary {
        private LocalDate startDate;
        private LocalDate endDate;
        private BigDecimal totalRevenue;
        private Long transactionCount;
        private BigDecimal averageTicket;
    }
}