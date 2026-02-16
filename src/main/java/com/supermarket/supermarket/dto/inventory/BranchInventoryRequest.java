package com.supermarket.supermarket.dto.inventory;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchInventoryRequest {
    @NotNull
    private Long branchId;
    @NotNull
    private Long productId;
    @NotNull
    @Min(0)
    private Integer stock;
    private Integer minStock;
}