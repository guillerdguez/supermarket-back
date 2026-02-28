package com.supermarket.supermarket.dto.transfer;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.supermarket.supermarket.model.transfer.TransferStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransferResponse {
    private Long id;
    private Long sourceBranchId;
    private String sourceBranchName;
    private Long targetBranchId;
    private String targetBranchName;
    private Long productId;
    private String productName;
    private Integer quantity;
    private TransferStatus status;
    private Long requestedById;
    private String requestedByUsername;
    private Long approvedById;
    private String approvedByUsername;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime requestedAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approvedAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completedAt;
    private String rejectionReason;
}