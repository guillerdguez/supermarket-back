package com.supermarket.supermarket.dto.cashregister;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CloseRegisterRequest {
    @NotNull(message = "Closing balance is required")
    @PositiveOrZero(message = "Closing balance must be zero or positive")
    private BigDecimal closingBalance;
}