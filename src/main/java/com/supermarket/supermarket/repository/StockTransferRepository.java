package com.supermarket.supermarket.repository;

import com.supermarket.supermarket.model.StockTransfer;
import com.supermarket.supermarket.model.TransferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockTransferRepository extends JpaRepository<StockTransfer, Long> {
    List<StockTransfer> findByStatus(TransferStatus status);

    List<StockTransfer> findBySourceBranchId(Long sourceBranchId);

    List<StockTransfer> findByTargetBranchId(Long targetBranchId);

    List<StockTransfer> findByRequestedById(Long requestedById);

    List<StockTransfer> findBySourceBranchIdAndStatus(Long sourceBranchId, TransferStatus status);

    List<StockTransfer> findByTargetBranchIdAndStatus(Long targetBranchId, TransferStatus status);
}