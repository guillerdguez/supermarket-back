package com.supermarket.supermarket.mapper;

import com.supermarket.supermarket.dto.transfer.TransferResponse;
import com.supermarket.supermarket.model.Branch;
import com.supermarket.supermarket.model.Product;
import com.supermarket.supermarket.model.StockTransfer;
import com.supermarket.supermarket.model.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TransferMapper {

    public TransferResponse toResponse(StockTransfer transfer) {
        if (transfer == null) return null;

        TransferResponse response = TransferResponse.builder()
                .id(transfer.getId())
                .quantity(transfer.getQuantity())
                .status(transfer.getStatus())
                .requestedAt(transfer.getRequestedAt())
                .approvedAt(transfer.getApprovedAt())
                .completedAt(transfer.getCompletedAt())
                .rejectionReason(transfer.getRejectionReason())
                .build();

        Branch source = transfer.getSourceBranch();
        if (source != null) {
            response.setSourceBranchId(source.getId());
            response.setSourceBranchName(source.getName());
        }

        Branch target = transfer.getTargetBranch();
        if (target != null) {
            response.setTargetBranchId(target.getId());
            response.setTargetBranchName(target.getName());
        }

        Product product = transfer.getProduct();
        if (product != null) {
            response.setProductId(product.getId());
            response.setProductName(product.getName());
        }

        User requestedBy = transfer.getRequestedBy();
        if (requestedBy != null) {
            response.setRequestedById(requestedBy.getId());
            response.setRequestedByUsername(requestedBy.getUsername());
        }

        User approvedBy = transfer.getApprovedBy();
        if (approvedBy != null) {
            response.setApprovedById(approvedBy.getId());
            response.setApprovedByUsername(approvedBy.getUsername());
        }

        return response;
    }

    public List<TransferResponse> toResponseList(List<StockTransfer> transfers) {
        if (transfers == null) return null;
        return transfers.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}