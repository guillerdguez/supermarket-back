package com.supermarket.supermarket.dto.inventory;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LowStockAlertResponse {
    private Long branchId;
    private String branchName;
    private Long productId;
    private String productName;
    private Integer currentStock;
    private Integer minStock;
}