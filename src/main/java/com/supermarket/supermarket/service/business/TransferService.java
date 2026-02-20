package com.supermarket.supermarket.service.business;

import com.supermarket.supermarket.dto.transfer.RejectTransferRequest;
import com.supermarket.supermarket.dto.transfer.TransferRequest;
import com.supermarket.supermarket.dto.transfer.TransferResponse;

import java.util.List;

public interface TransferService {
    TransferResponse requestTransfer(TransferRequest request);

    TransferResponse approveTransfer(Long transferId);

    TransferResponse rejectTransfer(Long transferId, RejectTransferRequest request);

    TransferResponse completeTransfer(Long transferId);

    TransferResponse cancelTransfer(Long transferId);

    List<TransferResponse> getAllTransfers();

    TransferResponse getTransferById(Long id);

    List<TransferResponse> getTransfersByStatus(String status);

    List<TransferResponse> getTransfersBySourceBranch(Long branchId);

    List<TransferResponse> getTransfersByTargetBranch(Long branchId);
}