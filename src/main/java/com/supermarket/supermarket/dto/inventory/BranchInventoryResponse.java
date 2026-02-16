package com.supermarket.supermarket.dto.inventory;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchInventoryResponse {
    private Long id;
    private Long branchId;
    private String branchName;
    private Long productId;
    private String productName;
    private String productCategory;
    private Integer stock;
    private Integer minStock;
    private LocalDateTime lastRestockDate;
}