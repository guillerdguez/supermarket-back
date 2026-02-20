package com.supermarket.supermarket.dto.cashregister;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.supermarket.supermarket.model.CashRegisterStatus;
import lombok.*;
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