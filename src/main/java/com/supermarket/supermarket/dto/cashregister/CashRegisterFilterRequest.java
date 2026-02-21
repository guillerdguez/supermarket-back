package com.supermarket.supermarket.dto.cashregister;

import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashRegisterFilterRequest {
    @PastOrPresent(message = "Start date cannot be in the future")
    private LocalDate startDate;
    @PastOrPresent(message = "End date cannot be in the future")
    private LocalDate endDate;
    private Long branchId;
    private boolean showOnlyDiscrepancies;
}