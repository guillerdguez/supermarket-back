package com.supermarket.supermarket.mapper;

import com.supermarket.supermarket.dto.transfer.TransferResponse;
import com.supermarket.supermarket.model.transfer.StockTransfer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TransferMapper {

    public TransferResponse toResponse(StockTransfer transfer) {
        if (transfer == null) return null;

        return TransferResponse.builder()
                .id(transfer.getId())
                .quantity(transfer.getQuantity())
                .status(transfer.getStatus())
                .requestedAt(transfer.getRequestedAt())
                .approvedAt(transfer.getApprovedAt())
                .completedAt(transfer.getCompletedAt())
                .rejectionReason(transfer.getRejectionReason())
                .sourceBranchId(transfer.getSourceBranch() != null ? transfer.getSourceBranch().getId() : null)
                .sourceBranchName(transfer.getSourceBranch() != null ? transfer.getSourceBranch().getName() : null)
                .targetBranchId(transfer.getTargetBranch() != null ? transfer.getTargetBranch().getId() : null)
                .targetBranchName(transfer.getTargetBranch() != null ? transfer.getTargetBranch().getName() : null)
                .productId(transfer.getProduct() != null ? transfer.getProduct().getId() : null)
                .productName(transfer.getProduct() != null ? transfer.getProduct().getName() : null)
                .requestedById(transfer.getRequestedBy() != null ? transfer.getRequestedBy().getId() : null)
                .requestedByUsername(transfer.getRequestedBy() != null ? transfer.getRequestedBy().getUsername() : null)
                .approvedById(transfer.getApprovedBy() != null ? transfer.getApprovedBy().getId() : null)
                .approvedByUsername(transfer.getApprovedBy() != null ? transfer.getApprovedBy().getUsername() : null)
                .build();
    }

    public List<TransferResponse> toResponseList(List<StockTransfer> transfers) {
        if (transfers == null) return null;
        return transfers.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}