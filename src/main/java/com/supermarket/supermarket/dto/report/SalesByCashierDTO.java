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
public class SalesByCashierDTO {
    private Long cashierId;
    private String cashierUsername;
    private BigDecimal totalRevenue;
    private Long transactionCount;
    private BigDecimal averageTicket;
}