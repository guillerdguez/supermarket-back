
package com.supermarket.supermarket.dto.saleDetail;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleDetailRequest {

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Minimum quantity allowed is 1")
    private Integer quantity;

    @NotNull(message = "Product ID is required")
    private Long productId;
    
}