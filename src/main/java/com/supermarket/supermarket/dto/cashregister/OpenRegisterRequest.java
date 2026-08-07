package com.supermarket.supermarket.dto.cashregister;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpenRegisterRequest {
    private Long branchId;

    @NotNull(message = "Opening balance is required")
    @Positive(message = "Opening balance must be positive")
    private BigDecimal openingBalance;
}