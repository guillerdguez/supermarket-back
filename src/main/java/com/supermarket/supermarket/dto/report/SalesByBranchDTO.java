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
public class SalesByBranchDTO {
    private Long branchId;
    private String branchName;
    private BigDecimal totalRevenue;
    private Long transactionCount;
}