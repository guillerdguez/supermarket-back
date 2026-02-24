package com.supermarket.supermarket.dto.sale;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.supermarket.supermarket.dto.saleDetail.SaleDetailResponse;
import com.supermarket.supermarket.model.CashRegisterStatus;
import com.supermarket.supermarket.model.SaleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SaleResponse {
    private Long id;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
    private SaleStatus status;
    private BigDecimal total;
    private Long branchId;
    private String branchName;
    private Long createdById;
    private String createdByUsername;
    private String createdByEmail;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    private Long cancelledById;
    private String cancelledByUsername;
    private String cancellationReason;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime cancelledAt;
    private List<SaleDetailResponse> details;
    private Long cashRegisterId;
    private CashRegisterStatus cashRegisterStatus;
}