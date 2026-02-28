package com.supermarket.supermarket.dto.cashregister;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.supermarket.supermarket.model.cashregister.CashRegisterStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashRegisterResponse {
    private Long id;
    private Long branchId;
    private String branchName;
    private BigDecimal openingBalance;
    private BigDecimal closingBalance;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime openingTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime closingTime;
    private CashRegisterStatus status;
    private Long openedById;
    private String openedByUsername;
    private Long closedById;
    private String closedByUsername;
}