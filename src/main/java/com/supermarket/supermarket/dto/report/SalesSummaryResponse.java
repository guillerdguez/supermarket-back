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
public class SalesSummaryResponse {
    private BigDecimal totalRevenue;
    private Long transactionCount;
    private BigDecimal averageTicket;
}