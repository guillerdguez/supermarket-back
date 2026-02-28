package com.supermarket.supermarket.fixtures.transfer;

import com.supermarket.supermarket.dto.transfer.RejectTransferRequest;
import com.supermarket.supermarket.dto.transfer.TransferRequest;
import com.supermarket.supermarket.dto.transfer.TransferResponse;
import com.supermarket.supermarket.fixtures.branch.BranchFixtures;
import com.supermarket.supermarket.fixtures.product.ProductFixtures;
import com.supermarket.supermarket.fixtures.user.UserFixtures;
import com.supermarket.supermarket.model.branch.Branch;
import com.supermarket.supermarket.model.transfer.StockTransfer;
import com.supermarket.supermarket.model.transfer.TransferStatus;

import java.time.LocalDateTime;

public class TransferFixtures {

    public static StockTransfer pendingTransfer() {
        return StockTransfer.builder()
                .id(1L)
                .sourceBranch(BranchFixtures.defaultBranch())
                .targetBranch(Branch.builder().id(2L).name("North Branch").address("456 North Ave").build())
                .product(ProductFixtures.defaultProduct())
                .quantity(10)
                .status(TransferStatus.PENDING)
                .requestedBy(UserFixtures.defaultCashier())
                .requestedAt(LocalDateTime.now())
                .build();
    }

    public static StockTransfer approvedTransfer() {
        StockTransfer t = pendingTransfer();
        t.setStatus(TransferStatus.APPROVED);
        t.setApprovedBy(UserFixtures.defaultManager());
        t.setApprovedAt(LocalDateTime.now());
        return t;
    }

    public static TransferRequest validTransferRequest() {
        return TransferRequest.builder()
                .sourceBranchId(1L)
                .targetBranchId(2L)
                .productId(1L)
                .quantity(10)
                .build();
    }

    public static TransferRequest sameBranchRequest() {
        return TransferRequest.builder()
                .sourceBranchId(1L)
                .targetBranchId(1L)
                .productId(1L)
                .quantity(10)
                .build();
    }

    public static RejectTransferRequest validRejectRequest() {
        return RejectTransferRequest.builder()
                .reason("Stock needed locally for upcoming promotion")
                .build();
    }

    public static TransferResponse transferResponse() {
        return TransferResponse.builder()
                .id(1L)
                .sourceBranchId(1L)
                .sourceBranchName("Central Branch")
                .targetBranchId(2L)
                .targetBranchName("North Branch")
                .productId(1L)
                .productName("Premium Rice")
                .quantity(10)
                .status(TransferStatus.PENDING)
                .requestedById(1L)
                .requestedByUsername("cashier-test")
                .requestedAt(LocalDateTime.now())
                .build();
    }
}