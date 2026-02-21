package com.supermarket.supermarket.dto.report;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashRegisterReportResponse {
    private Long totalClosures;
    private BigDecimal totalSurplus;
    private BigDecimal totalShortage;
    private List<ClosureDiscrepancyDTO> discrepancies;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClosureDiscrepancyDTO {
        private Long registerId;
        private Long branchId;
        private String branchName;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime openingTime;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime closingTime;
        private String openedBy;
        private String closedBy;
        private BigDecimal expectedAmount;
        private BigDecimal actualClosingAmount;
        private BigDecimal varianceAmount;
    }
}