package com.supermarket.supermarket.dto.sale;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.supermarket.supermarket.dto.saleDetail.SaleDetailRequest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleRequest {

    @PastOrPresent(message = "Sale date cannot be in the future")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @NotNull(message = "Branch ID is required")
    private Long branchId;

    @Valid
    @NotNull(message = "Sale details are required")
    @Size(min = 1, message = "Must contain at least one sale detail")
    private List<SaleDetailRequest> details;
}